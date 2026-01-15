# Chi Tiết Kiến Trúc Dự Án

## Tổng Quan

Dự án sử dụng **Hexagonal Architecture (Ports & Adapters)** kết hợp với **Domain-Driven Design (DDD)** và **CQRS pattern**. Kiến trúc được thiết kế để đảm bảo:
- **Tách biệt các module** (auth, user, product, stock, order)
- **Độc lập domain logic** khỏi infrastructure
- **Dễ test và maintain**
- **Scalability và flexibility**

---

## Cấu Trúc Tổng Thể

```
system-backend/
├── common/                    # Shared kernel - Infrastructure chung
│   ├── config/               # Configuration (Security, CORS, JWT, etc.)
│   ├── enums/                # Shared enums (UserRole, UserStatus, etc.)
│   ├── exception/            # Exception hierarchy & Global handler
│   ├── port/                 # Port interfaces cho cross-module communication
│   ├── response/             # Standard response wrappers
│   └── security/             # Security infrastructure (JWT, filters)
│
├── auth/                      # Authentication Module
├── user/                      # User Management Module
├── product/                   # Product Management Module
├── stock/                     # Stock Management Module
├── order/                     # Order Management Module (chưa implement)
└── otp/                       # OTP Service Module
```

---

## Kiến Trúc Chi Tiết Từng Module

### 1. Module Structure Pattern

Mỗi module (auth, user, product) tuân theo cấu trúc layers:

```
module/
├── adapter/              # Adapters - Implement ports từ common
├── application/          # Application Layer
│   ├── facade/          # Orchestration services (cross-domain)
│   └── service/         # Application services (single domain)
├── controller/          # Presentation Layer (REST API)
├── domain/              # Domain Layer (business logic)
├── dto/                 # Data Transfer Objects
├── entity/              # Domain entities (JPA)
├── mapper/              # Object mapping (Entity <-> DTO)
└── repository/          # Data access (JPA repositories)
```

---

## Layer Responsibilities

### 1. **Controller Layer** (Presentation)

**Trách nhiệm:**
- Nhận HTTP requests
- Validate input cơ bản
- Gọi Facade/Service
- Trả về HTTP responses

**Quy tắc:**
- ❌ KHÔNG được gọi Repository trực tiếp
- ❌ KHÔNG được gọi Entity trực tiếp
- ✅ CHỈ gọi Facade hoặc Application Service
- ✅ CHỈ làm việc với DTO

**Ví dụ:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserFacade userFacade;
    
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getUser(@PathVariable Integer userId) {
        UserProfileResponse user = userFacade.getUserProfileById(userId);
        return ResponseEntity.ok(SuccessResponse.success(user));
    }
}
```

---

### 2. **Facade Layer** (Application - Orchestration)

**Trách nhiệm:**
- **Orchestrate cross-domain operations** (phối hợp nhiều domain)
- Coordinate giữa nhiều services
- Transaction management cho complex workflows
- Mapping giữa domain objects và DTOs

**Quy tắc:**
- ✅ Gọi nhiều Command/Query Services
- ✅ Gọi services từ modules khác (qua Port nếu cần)
- ✅ Xử lý transaction boundaries
- ❌ KHÔNG chứa business logic (delegate to domain services)

**Ví dụ:**
```java
@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserMapper userMapper;
    
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        // Orchestrate: Create user + Create auth provider
        User savedUser = userCommandService.createUser(request);
        userCommandService.createAuthProvider(savedUser.getUserId(), 
                                             request.getEmail(), 
                                             request.getPassword());
        return userMapper.mapToUserProfileResponse(savedUser);
    }
}
```

---

### 3. **Application Service Layer** (CQRS)

Chia thành 2 loại services:

#### **Command Service** (Write Operations)
**Trách nhiệm:**
- Xử lý write operations (create, update, delete)
- Gọi Domain Validation Services
- Persist data qua Repository

**Quy tắc:**
- ✅ CHỈ xử lý 1 aggregate/entity
- ✅ Delegate validation to Domain Service
- ❌ KHÔNG orchestrate cross-domain operations

**Ví dụ:**
```java
@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordManagementPort passwordManagementPort;
    
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Validate using domain service
        userValidationService.validateEmail(request.getEmail());
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        
        return userRepository.save(user);
    }
}
```

#### **Query Service** (Read Operations)
**Trách nhiệm:**
- Xử lý read operations
- Fetch data từ Repository
- Apply filters, sorting, pagination

**Quy tắc:**
- ✅ CHỈ read operations
- ✅ Return domain entities (không map ở đây)
- ❌ KHÔNG modify data

**Ví dụ:**
```java
@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;
    
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
    public Page<User> getUsersRaw(String email, String username, 
                                  UserRole role, UserStatus status, 
                                  Pageable pageable) {
        return userRepository.findByFilters(email, username, role, status, pageable);
    }
}
```

---

### 4. **Domain Service Layer** (Business Logic)

**Trách nhiệm:**
- Chứa **TOÀN BỘ business logic và validation**
- Domain rules và invariants
- Business operations phức tạp

**Quy tắc:**
- ✅ Pure business logic
- ✅ Stateless
- ✅ Reusable across application services
- ❌ KHÔNG gọi Repository
- ❌ KHÔNG gọi external services

**Ví dụ:**
```java
@Service
public class UserValidationService {
    
    public void changeUserStatus(User user, String statusStr) {
        UserStatus newStatus = UserStatus.parseStatus(statusStr);
        
        // Business rule: Cannot change status of deleted user
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ValidationException("Cannot change status of deleted user");
        }
        
        user.setStatus(newStatus);
    }
    
    public void promoteUserToAdmin(User user) {
        // Business rule: Only CUSTOMER can be promoted
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ValidationException("Only customers can be promoted to admin");
        }
        user.setRole(UserRole.ADMIN);
    }
}
```

---

### 5. **Adapter Layer** (Hexagonal Architecture)

**Trách nhiệm:**
- Implement Port interfaces từ `common/port/`
- Cho phép modules khác sử dụng functionality mà không phụ thuộc trực tiếp

**Quy tắc:**
- ✅ Implement Port interface
- ✅ Delegate to domain services
- ✅ Handle cross-module communication

**Ví dụ:**
```java
@Component
@RequiredArgsConstructor
public class PasswordManagementAdapter implements PasswordManagementPort {
    private final AuthProviderRepository authProviderRepository;
    private final AuthValidationService authValidationService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void changePassword(Integer userId, String currentPassword, 
                              String newPassword, String confirmPassword) {
        authValidationService.validatePasswordConfirmation(newPassword, confirmPassword);
        
        AuthProvider authProvider = authProviderRepository
            .findByUserIdAndProvider(userId, AuthProvider.Provider.LOCAL)
            .orElseThrow(() -> new ValidationException("No local password"));
        
        if (!authValidationService.verifyAuthProviderPassword(authProvider, 
                                                              currentPassword, 
                                                              passwordEncoder)) {
            throw new ValidationException("Invalid current password");
        }
        
        authValidationService.updateAuthProviderPassword(authProvider, 
                                                        newPassword, 
                                                        passwordEncoder);
        authProviderRepository.save(authProvider);
    }
}
```

---

### 6. **Port Layer** (Common Module)

**Trách nhiệm:**
- Define interfaces cho cross-module communication
- Tách biệt modules khỏi nhau
- Tuân thủ **Interface Segregation Principle** (ISP)

**Quy tắc thiết kế Port:**
- ✅ Chia nhỏ theo use case (Query/Command)
- ✅ Chỉ expose những gì consumer cần
- ✅ Không expose implementation details (như Repository methods)
- ❌ Không tạo "god interface" chứa tất cả methods

**Ví dụ 1: Password Management Port**
```java
// common/port/PasswordManagementPort.java
public interface PasswordManagementPort {
    void createAuthProviderWithPassword(Integer userId, String email, String password);
    void changePassword(Integer userId, String currentPassword, 
                       String newPassword, String confirmPassword);
    void resetPassword(Integer userId, String email);
}

// user module sử dụng:
@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final PasswordManagementPort passwordManagementPort; // Không phụ thuộc auth module
    
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        passwordManagementPort.changePassword(userId, 
                                             request.getCurrentPassword(),
                                             request.getNewPassword(),
                                             request.getConfirmPassword());
    }
}
```

**Ví dụ 2: User Query/Command Ports (CQRS)**
```java
// common/port/UserQueryPort.java
public interface UserQueryPort {
    Optional<UserPort> findById(Integer userId);
    Optional<UserPort> findByEmail(String email);
    boolean existsByEmail(String email);
}

// common/port/UserCommandPort.java
public interface UserCommandPort {
    UserPort createUserWithEmail(String email, String fullName);
    UserPort createUserWithoutEmail();
    UserPort updateUserProfile(UserPort user, String email, String fullName);
    UserPort saveUser(UserPort user);
}

// common/port/UserPort.java (Data interface)
public interface UserPort {
    Integer getUserId();
    String getEmail();
    String getFullName();
    UserRole getRole();
    UserStatus getStatus();
    LocalDateTime getCreatedAt();
}

// auth module sử dụng:
@Service
@RequiredArgsConstructor
public class AuthQueryService {
    private final UserQueryPort userQueryPort; // Chỉ thấy query methods
    
    public UserPort getUserById(Integer userId) {
        return userQueryPort.findById(userId)
            .orElseThrow(AuthenticationException::invalidCredentials);
    }
}

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final UserCommandPort userCommandPort; // Chỉ thấy command methods
    
    public UserPort createUser(String email, String fullName) {
        return userCommandPort.createUserWithEmail(email, fullName);
    }
}
```

**Tại sao không dùng UserRepositoryPort?**
```java
// ❌ BAD: Expose repository interface
public interface UserRepositoryPort {
    Optional<UserPort> findById(Integer userId);
    Optional<UserPort> findByEmail(String email);
    boolean existsByEmail(String email);
    UserPort save(UserPort user);  // Mix query & command!
}

// ✅ GOOD: Chia theo CQRS
public interface UserQueryPort {
    // Chỉ read operations
}

public interface UserCommandPort {
    // Chỉ write operations
}
```

**Lợi ích:**
- Auth module không biết về Repository pattern
- Dễ mock và test
- Tuân thủ ISP - mỗi consumer chỉ depend vào những gì cần
- Rõ ràng về intent (Query vs Command)

---

## Module-Specific Architectures

### 1. **Auth Module**

```
auth/
├── adapter/
│   └── PasswordManagementAdapter.java      # Implement PasswordManagementPort
├── application/
│   ├── facade/
│   │   └── AuthFacade.java                 # Orchestrate: Auth + User + OTP
│   └── service/
│       ├── AuthCommandService.java         # Auth write operations
│       ├── AuthQueryService.java           # Auth read operations
│       └── GoogleAuthService.java          # Google OAuth integration
├── controller/
│   └── AuthController.java                 # REST endpoints
├── domain/
│   └── AuthValidationService.java          # Auth business logic
├── dto/                                     # Request/Response DTOs
├── entity/
│   └── AuthProvider.java                   # Auth entity
├── mapper/
│   └── AuthMapper.java                     # Entity <-> DTO mapping
└── repository/
    └── AuthProviderRepository.java         # Data access
```

**Đặc điểm:**
- Sử dụng **UserPort** để tránh phụ thuộc trực tiếp vào User entity
- Orchestrate với OTP service cho phone authentication
- Integrate với Google OAuth

---

### 2. **User Module**

```
user/
├── adapter/
│   ├── UserCommandAdapter.java             # Implement UserCommandPort
│   └── UserQueryAdapter.java               # Implement UserQueryPort
├── application/
│   ├── facade/
│   │   └── UserFacade.java                 # Orchestrate: User + Auth
│   └── service/
│       ├── UserCommandService.java         # User write operations
│       └── UserQueryService.java           # User read operations
├── controller/
│   ├── UserController.java                 # User endpoints
│   └── AdminUserController.java            # Admin endpoints
├── domain/
│   └── UserValidationService.java          # User business logic
├── dto/                                     # Request/Response DTOs
├── entity/
│   └── User.java                           # User entity
├── mapper/
│   └── UserMapper.java                     # Entity <-> DTO mapping
└── repository/
    └── UserRepository.java                 # Data access
```

**Đặc điểm:**
- Sử dụng **PasswordManagementPort** để delegate password operations
- Implement **UserPort** cho auth module
- Tách biệt admin và user controllers

---

### 3. **Product Module** (Complex Nested Structure)

```
product/
├── application/
│   ├── facade/
│   │   └── ProductFacade.java              # Orchestrate: Product + Book + Category
│   └── service/
│       ├── ProductCommandService.java      # Product write operations
│       └── ProductQueryService.java        # Product read operations
├── book/                                    # Sub-module: Book
│   ├── domain/
│   │   └── BookValidationService.java
│   ├── dto/
│   ├── entity/
│   │   └── Book.java
│   ├── mapper/
│   │   └── BookMapper.java
│   ├── repository/
│   │   └── BookRepository.java
│   └── service/
│       └── BookService.java                # Book operations
├── category/                                # Sub-module: Category
│   ├── application/
│   │   ├── facade/
│   │   │   └── CategoryFacade.java
│   │   └── service/
│   │       ├── CategoryCommandService.java
│   │       └── CategoryQueryService.java
│   ├── controller/
│   │   └── CategoryController.java
│   ├── domain/
│   │   └── CategoryValidationService.java
│   ├── dto/
│   ├── entity/
│   │   └── Category.java
│   ├── mapper/
│   │   └── CategoryMapper.java
│   └── repository/
│       └── CategoryRepository.java
├── image/                                   # Sub-module: Product Image
│   ├── application/
│   │   └── service/
│   │       ├── ProductImageCommandService.java
│   │       └── ProductImageQueryService.java
│   ├── controller/
│   │   └── ProductImageController.java
│   ├── domain/
│   │   └── ProductImageValidationService.java
│   ├── dto/
│   ├── entity/
│   │   └── ProductImage.java
│   ├── mapper/
│   │   └── ProductImageMapper.java
│   └── repository/
│       └── ProductImageRepository.java
├── controller/
│   └── ProductController.java
├── domain/
│   └── ProductValidationService.java
├── dto/
├── entity/
│   ├── Product.java
│   ├── ProductCategory.java                # Join table entity
│   └── ProductCategoryId.java              # Composite key
├── mapper/
│   ├── ProductMapper.java
│   └── ProductCategoryMapper.java
└── repository/
    ├── ProductRepository.java
    └── ProductCategoryRepository.java
```

**Đặc điểm:**
- **Nested sub-modules**: book, category, image
- ProductFacade orchestrate giữa Product, Book, Category
- Category có tree structure (parent-child relationships)
- Product-Category là many-to-many relationship

---

### 4. **Common Module** (Shared Kernel)

```
common/
├── config/
│   ├── SecurityConfig.java                 # Spring Security configuration
│   ├── JwtProperties.java                  # JWT settings
│   ├── GoogleProperties.java               # Google OAuth settings
│   ├── CorsConfig.java                     # CORS configuration
│   ├── PasswordConfig.java                 # Password encoder bean
│   └── FileUploadProperties.java           # File upload settings
├── enums/
│   ├── UserRole.java                       # ADMIN, CUSTOMER
│   ├── UserStatus.java                     # ACTIVE, INACTIVE, BANNED, DELETED
│   └── CategoryStatus.java                 # ACTIVE, INACTIVE
├── exception/
│   ├── BaseException.java                  # Base exception class
│   ├── BusinessException.java              # Business logic exceptions
│   ├── ValidationException.java            # Validation errors
│   ├── ResourceNotFoundException.java      # 404 errors
│   ├── DuplicateResourceException.java     # Duplicate errors
│   ├── AuthenticationException.java        # Auth errors
│   ├── SecurityException.java              # Security errors
│   ├── IntegrationException.java           # External service errors
│   ├── SystemException.java                # System errors
│   └── GlobalExceptionHandler.java         # @ControllerAdvice handler
├── port/
│   ├── PasswordManagementPort.java         # Password operations interface
│   ├── UserPort.java                       # User data interface
│   ├── UserQueryPort.java                  # User query interface
│   ├── UserCommandPort.java                # User command interface
│   └── UserRepositoryPort.java             # User repository interface
├── response/
│   ├── SuccessResponse.java                # Standard success wrapper
│   ├── ErrorResponse.java                  # Standard error wrapper
│   └── PageResponse.java                   # Pagination wrapper
└── security/
    ├── JwtService.java                     # JWT generation/validation
    ├── JwtAuthenticationFilter.java        # JWT filter
    ├── TokenBlacklistService.java          # Token blacklist
    ├── CustomAuthenticationEntryPoint.java # 401 handler
    └── CustomAccessDeniedHandler.java      # 403 handler
```

---

## Dependency Rules (Architecture Tests)

Dự án sử dụng **ArchUnit** để enforce architecture rules:

```java
@Test
void modulesShouldNotDependOnEachOther() {
    // auth, product, user, order, stock modules KHÔNG được phụ thuộc lẫn nhau
    // Chỉ được giao tiếp qua common/port/
}

@Test
void controllersShouldNotDependOnRepositories() {
    // Controller KHÔNG được gọi Repository trực tiếp
}

@Test
void controllersShouldNotDependOnEntities() {
    // Controller KHÔNG được làm việc với Entity trực tiếp
}

@Test
void dtosShouldNotDependOnEntities() {
    // DTO KHÔNG được reference Entity
}
```

---

## Communication Patterns

### 1. **Intra-Module Communication** (Trong cùng module)
```
Controller -> Facade -> Command/Query Service -> Domain Service -> Repository
```

### 2. **Cross-Module Communication** (Giữa các module)
```
Module A -> Port Interface (common) <- Adapter (Module B)
```

**Ví dụ:**
```
UserCommandService -> PasswordManagementPort <- PasswordManagementAdapter (auth module)
```

### 3. **Facade Orchestration** (Phối hợp nhiều domains)
```
UserFacade:
  - UserCommandService.createUser()
  - PasswordManagementPort.createAuthProvider()
  - UserMapper.mapToResponse()
```

---

## Design Patterns Được Sử Dụng

### 1. **Hexagonal Architecture (Ports & Adapters)**
- **Ports**: Interfaces trong `common/port/`
- **Adapters**: Implementations trong `module/adapter/`
- **Benefit**: Tách biệt modules, dễ test, dễ thay đổi implementation

### 2. **CQRS (Command Query Responsibility Segregation)**
- **Command Services**: Write operations
- **Query Services**: Read operations
- **Benefit**: Tách biệt read/write logic, optimize riêng biệt

### 3. **Facade Pattern**
- **Facade Layer**: Orchestrate complex workflows
- **Benefit**: Simplify client code, hide complexity

### 4. **Repository Pattern**
- **Repository Layer**: Abstract data access
- **Benefit**: Tách biệt domain khỏi persistence

### 5. **Mapper Pattern**
- **Mapper Layer**: Convert Entity <-> DTO
- **Benefit**: Tách biệt internal/external representations

### 6. **Strategy Pattern**
- **Domain Services**: Encapsulate business rules
- **Benefit**: Reusable, testable business logic

---

## Best Practices

### 1. **Separation of Concerns**
- Mỗi layer có trách nhiệm rõ ràng
- Không vi phạm layer boundaries

### 2. **Dependency Inversion**
- Depend on abstractions (Ports), not implementations
- High-level modules không depend on low-level modules

### 3. **Single Responsibility**
- Mỗi service chỉ xử lý 1 aggregate/entity
- Facade xử lý orchestration

### 4. **Domain-Driven Design**
- Business logic tập trung trong Domain Services
- Entities chứa domain data và behavior

### 5. **Testability**
- Pure business logic dễ unit test
- Ports cho phép mock dependencies

---

## Testing Strategy

### 1. **Unit Tests**
- Test Domain Services (pure business logic)
- Test Mappers
- Test Validation logic

### 2. **Integration Tests**
- Test Repository layer
- Test API endpoints
- Test cross-module communication

### 3. **Architecture Tests** (ArchUnit)
- Enforce dependency rules
- Validate layer boundaries
- Ensure naming conventions

---

## Migration Path & Evolution

### Current State:
- ✅ Auth module: Full Hexagonal + CQRS
- ✅ User module: Full Hexagonal + CQRS
- ✅ Product module: Full Hexagonal + CQRS (with nested sub-modules)
- 🚧 Stock module: Structure created, not implemented
- 🚧 Order module: Structure created, not implemented
- ⚠️ OTP module: Simple structure (entity + repository + service)

### Future Improvements:
1. Migrate OTP module to full architecture
2. Implement Stock module
3. Implement Order module
4. Add Event-Driven communication (Spring Events)
5. Add Saga pattern for distributed transactions

---

## Kết Luận

Kiến trúc này cung cấp:
- ✅ **Modularity**: Modules độc lập, dễ phát triển song song
- ✅ **Maintainability**: Code rõ ràng, dễ maintain
- ✅ **Testability**: Dễ test từng layer
- ✅ **Scalability**: Dễ mở rộng thêm modules
- ✅ **Flexibility**: Dễ thay đổi implementation
- ✅ **Clean Code**: Tuân thủ SOLID principles

Đây là một kiến trúc enterprise-grade, phù hợp cho dự án lớn và phức tạp.
