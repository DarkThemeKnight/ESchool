package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.dto.PermissionOutDto;
import com.lucumasystems.authenticationapi.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @Query("SELECT new com.lucumasystems.authenticationapi.dto.PermissionOutDto(p.name,p.description) as description FROM Permission p WHERE UPPER(p.name) = UPPER(:name) AND p.active = true")
    Optional<PermissionOutDto> findByNameAndActive(@Param("name") String name);
    boolean existsByName(String name);
    @Query("SELECT p FROM Permission p WHERE p.active = true AND (p.id IN :pid OR p.name IN :names) ORDER BY p.createdAt DESC")
    Page<Permission> findPermissionsFromPermissionsIdOrPermissionsNames(
            Pageable pageable,
            @Param("pid") List<Integer> permissionsIds,
            @Param("names") List<String> names);
    @Query("SELECT new com.lucumasystems.authenticationapi.dto.PermissionOutDto(p.name,p.description) FROM Permission p WHERE p.active = true ORDER BY p.createdAt DESC")
    Page<PermissionOutDto> findAllPaged(Pageable pageable);
    @Query("SELECT new com.lucumasystems.authenticationapi.dto.PermissionOutDto(p.name,p.description) FROM Role r INNER JOIN r.permissions p WHERE r.name = :role AND p.active = true ORDER BY p.createdAt DESC")
    Page<PermissionOutDto> findAllPagedRoleFilter(Pageable pageable, @Param("role") String role);

}
