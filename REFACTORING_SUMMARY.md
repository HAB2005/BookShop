# Refactoring Summary - Architecture Violations Fix

## Mục tiêu
Sửa các vi phạm kiến trúc được phát hiện bởi ArchUnit tests:
1. **Circular Dependency** giữa module `auth` và `user`
2. **Module Dependency** - các module phụ thuộc lẫn nhau
3. **DTO depends on Entity** - DTO phụ thuộc vào Entity enum

## Các thay đổi đã thực hiện

### 1. Tạo Shared Enums trong Common Package

#### Tạo mới:
- `common/enums/CategoryStatus.java` - Enum cho category status (ACTIVE, INACTIVE)
- `common/enums/UserRole.java` - Enum cho user role (customer, admin)
- `common/enums/UserStatus.java` - Enum cho user status (active, inactive, banned)

**Lý do**: Tách enum ra khỏi Entity để DTO có thể sử dụng mà không phụ thuộc vào Entity class.

### 2. Tạo Port Interface để Breaking Circular Dependency

#### Tạo mới:
- `common/port/UserPort.java` - Interface định nghĩa contract cho User data
- `common/port/UserRepositoryPort.java` - Interface cho User repository operations

**Lý do**: Auth module có thể sử dụng UserPort interface thay vì phụ thuộc trực tiếp vào User entity, phá vỡ circular dependency.

### 3. Cập nhật Entity Classes

#### `user/entity/User.java`:
- Implement `UserPort` interface
- Thay đổi field type từ `User.Role` → `UserRole`
- Thay đổi field type từ `User.Status` → `UserStatus`
- Giữ lại inner enum `Role` và `Status` với annotation `@Deprecated` để backward compatibility

#### `auth/entity/AuthProvider.java`:
- **XÓA** relationship `@ManyToOne` với User entity
- **XÓA** field `private User user`
- Chỉ giữ lại `userId` field

**Lý do**: Loại bỏ bidirectional relationship giữa AuthProvider và User, phá vỡ circular dependency.

#### `product/category/entity/Category.java`:
- Thay đổi field type từ `Category.Status` → `CategoryStatus`
- **XÓA** inner enum `Status`

### 4. Cập nhật Domain Services

#### `auth/domain/AuthValidationService.java`:
- Thay đổi parameter type từ `User` → `UserPort`
- Import `UserStatus` từ common package
- Không còn phụ thuộc vào `user.entity.User`

#### `user/domain/UserValidationService.java`:
- Import và sử dụng `UserRole`, `UserStatus` từ common package
- Cập nhật tất cả method signatures

### 5. Cập nhật Application Services

#### `auth/application/service/AuthCommandService.java`:
- Import `UserRole`, `UserStatus` từ common package
- Cập nhật code sử dụng enum mới

#### `user/application/service/UserCommandService.java`:
- Import `UserRole`, `UserStatus` từ common package
- Cập nhật enum parsing logic

#### `user/application/service/UserQueryService.java`:
- Thay đổi method signature từ `User.Role, User.Status` → `UserRole, UserStatus`

### 6. Cập nhật Repositories

#### `user/repository/UserRepository.java`:
- Thay đổi method signatures sử dụng `UserRole`, `UserStatus`

#### `product/category/repository/CategoryRepository.java`:
- Thay đổi method signatures sử dụng `CategoryStatus`

### 7. Cập nhật Mappers

#### `auth/mapper/AuthMapper.java`:
- Thay đổi parameter type từ `User` → `UserPort`
- Không còn phụ thuộc vào User entity

### 8. Cập nhật DTOs

#### `product/category/dto/CategoryDTO.java`:
- Import và sử dụng `CategoryStatus` thay vì `Category.Status`

#### `product/category/dto/UpdateCategoryStatusRequest.java`:
- Import và sử dụng `CategoryStatus` thay vì `Category.Status`

### 9. Cập nhật Query Services

#### `product/category/application/service/CategoryQueryService.java`:
- Import và sử dụng `CategoryStatus`

#### `product/category/application/service/CategoryCommandService.java`:
- Import và sử dụng `CategoryStatus`

### 10. Cập nhật Validation Services

#### `product/category/domain/CategoryValidationService.java`:
- Import và sử dụng `CategoryStatus`

### 11. Cập nhật Facades

#### `user/application/facade/UserFacade.java`:
- Import và sử dụng `UserRole`, `UserStatus`

#### `product/category/application/facade/CategoryFacade.java`:
- Import và sử dụng `CategoryStatus`

## Kết quả

### Vi phạm đã sửa:

1. ✅ **Circular Dependency giữa auth ↔ user**: 
   - AuthProvider không còn relationship với User entity
   - AuthValidationService sử dụng UserPort interface thay vì User entity
   - Auth module không còn phụ thuộc trực tiếp vào user module

2. ✅ **Module Dependency**: 
   - Các module sử dụng shared enums từ common package
   - Không còn cross-module entity dependencies

3. ✅ **DTO depends on Entity**: 
   - DTOs sử dụng enum từ common package
   - Không còn phụ thuộc vào Entity inner enums

## Kiến trúc sau refactoring

```
common/
  ├── enums/
  │   ├── CategoryStatus
  │   ├── UserRole
  │   └── UserStatus
  └── port/
      ├── UserPort (interface)
      └── UserRepositoryPort (interface)

auth/
  ├── entity/AuthProvider (no User relationship)
  ├── domain/AuthValidationService (uses UserPort)
  └── mapper/AuthMapper (uses UserPort)

user/
  └── entity/User (implements UserPort, uses shared enums)

product/category/
  ├── entity/Category (uses CategoryStatus)
  └── dto/ (uses CategoryStatus)
```

## Backward Compatibility

- User entity vẫn giữ inner enum `Role` và `Status` với `@Deprecated` annotation
- Code cũ vẫn compile được nhưng sẽ có warning
- Khuyến nghị migrate dần sang sử dụng enum từ common package

## Testing

Chạy lại architecture tests:
```bash
mvn test -Dtest=DependencyRuleTest
```

Tất cả tests nên pass sau refactoring này.
