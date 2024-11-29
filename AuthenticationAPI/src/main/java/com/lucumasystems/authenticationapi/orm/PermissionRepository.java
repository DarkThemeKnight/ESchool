package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @Query("SELECT p FROM Permission p WHERE UPPER(p.name) = UPPER(:name) AND p.active = true")
    Optional<Permission> findByNameAndActive(@Param("name") String name);
    boolean existsByName(String name);
    @Query("SELECT p FROM Permission p WHERE p.active = true AND (p.id IN :pid OR p.name IN :names) ORDER BY p.createdAt DESC")
    Page<Permission> findPermissionsFromPermissionsIdOrPermissionsNames(
            Pageable pageable,
            @Param("pid") List<Integer> permissionsIds,
            @Param("names") List<String> names);


}
