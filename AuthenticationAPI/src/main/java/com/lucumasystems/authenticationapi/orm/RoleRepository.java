package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.entity.Permission;
import com.lucumasystems.authenticationapi.entity.Role;
import com.lucumasystems.authenticationapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query("SELECT r FROM Role r WHERE r.name = :name AND r.isActive = true")
    Optional<Role> findByNameAndIsActive(@Param("name") String name);

    @Query("SELECT r FROM Role r WHERE r.isActive = true AND (r.id IN :rid OR r.name IN :names) ORDER BY r.createdAt DESC")
    Page<Role> listRoles(
            Pageable pageable,
            @Param("rid") List<Integer> roleIds,
            @Param("names") List<String> names);

    @Query("SELECT r FROM Role r INNER JOIN r.permissions p WHERE p.id IN :permissionId")
    Page<Role> findRolesWithPermissions(@Param("permissionId") List<Integer> permissionIds, Pageable pageable);

    @Query("SELECT r FROM Role r INNER JOIN r.permissions p WHERE p.name IN :permissionNames")
    Page<Role> findRolesWithPermissionNames(@Param("permissionNames") List<String> permissionNames, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.enabled = true AND r.name IN :roleNames")
    Page<User> findUsersWithRoleNames(@Param("roleNames") List<String> roleNames, Pageable pageable);

    @Query("SELECT r FROM Role r LEFT JOIN r.permissions p WHERE (:search IS NULL OR (r.name LIKE CONCAT('%',:search,'%') OR p.name LIKE CONCAT('%',:search,'%')))")
    Page<Role> findRolesWithSearch(@Param("search") String search, Pageable pageable);


    List<Role> findAllByNameIn(List<String> roles);
}
