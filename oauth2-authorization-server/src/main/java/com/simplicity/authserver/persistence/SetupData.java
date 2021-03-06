package com.simplicity.authserver.persistence;

import com.simplicity.authserver.persistence.domain.Privilege;
import com.simplicity.authserver.persistence.domain.Role;
import com.simplicity.authserver.persistence.domain.User;
import com.simplicity.authserver.persistence.domain.UserInfo;
import com.simplicity.authserver.persistence.repositories.PrivilegeRepository;
import com.simplicity.authserver.persistence.repositories.RoleRepository;
import com.simplicity.authserver.persistence.repositories.UserInfoRepository;
import com.simplicity.authserver.persistence.repositories.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Profile("!test")
public class SetupData implements InitializingBean {

    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final RoleRepository roleRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder encoder;

    public SetupData(UserRepository userRepository,
                     PrivilegeRepository privilegeRepository,
                     RoleRepository roleRepository,
                     UserInfoRepository userInfoRepository,
                     PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.privilegeRepository = privilegeRepository;
        this.roleRepository = roleRepository;
        this.userInfoRepository = userInfoRepository;
        this.encoder = encoder;
    }

    public void init() {
        initPrivileges();
        initRoles();
        initUsers();
    }

//    @Override
//    public void afterPropertiesSet() {
//        init();
//    }
//
//    private void shutdown() {
//        //TODO: destroy code
//    }
//
//
//    @Override
//    public void destroy() throws Exception {
//        shutdown();
//    }

    private void initUsers() {
        if (userRepository.findUserByUsername("pippo") == null) {
            Role adminRole = roleRepository.findAll().stream().filter(role -> role.getName().equals("ROLE_ADMIN")).findFirst().orElseThrow(EntityNotFoundException::new);
            Role userRole = roleRepository.findAll().stream().filter(role -> role.getName().equals("ROLE_USER")).findFirst().orElseThrow(EntityNotFoundException::new);
//            Role oauth2UserRole = roleRepository.findAll().stream().filter(role -> role.getName().equals("OAuth2Authentication_ROLE_USER")).findFirst().orElseThrow(EntityNotFoundException::new);
            User user1 = new User();
            user1.setUsername("pippo");
            user1.setPassword(encoder.encode("123"));
//            user1.setRoles(new LinkedHashSet<>(Arrays.asList(adminRole, userRole, oauth2UserRole)));
            user1.setRoles(new LinkedHashSet<>(Arrays.asList(adminRole, userRole)));
            userRepository.save(user1);
            UserInfo userInfo1 = new UserInfo();
            userInfo1.setName("Davide");
            userInfo1.setTelephone("00000");
            userInfoRepository.save(userInfo1);
            user1.setUserInfo(userInfo1);
            userRepository.save(user1);
        }
//        if(userRepository.findUserByUsername("admin") == null) {
//            Role adminRole = roleRepository.findAll().stream().filter(role-> role.getName().equals("ROLE_ADMIN")).findFirst().orElseThrow(EntityNotFoundException::new);
//            User user1 = new User();
//            user1.setUsername("admin");
//            user1.setPassword(encoder.encode("123"));
//            user1.setRoles(new LinkedHashSet<>(Arrays.asList(adminRole)));
//            userRepository.save(user1);
//            UserInfo userInfo1 = new UserInfo();
//            userInfo1.setName("admin");
//            userInfo1.setTelephone("00000");
//            userInfoRepository.save(userInfo1);
//            user1.setUserInfo(userInfo1);
//            userRepository.save(user1);
//        }

//        User user2 = new User();
//        user2.setUsername("tom");
//        user2.setPassword(encoder.encode("111"));
//        user2.setPrivileges(new HashSet<>(Arrays.asList(privilege1, privilege2)));
//        User savedUser2 = userRepository.save(user2);
//        savedUser2.getUserInfo().setUser(savedUser2);
//        savedUser2.getUserInfo().setName("johnName");
//        userRepository.save(savedUser2);
    }

    private void initPrivileges() {
        createPrivilegeIfNotFound("READ_PRIVILEGE");
        createPrivilegeIfNotFound("WRITE_PRIVILEGE");
    }

    private void initRoles() {
        Privilege readPrivilege
                = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege writePrivilege
                = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        Set<Privilege> privileges = new LinkedHashSet<>(Arrays.asList(readPrivilege, writePrivilege));
        createRoleIfNotFound("ROLE_ADMIN", privileges);
        createRoleIfNotFound("ROLE_USER", Collections.singleton(readPrivilege));
//        createRoleIfNotFound("OAuth2Authentication_ROLE_USER", privileges);

    }

    private Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege();
            privilege.setName(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    //    @Transactional
//    public User createUserIfNotFound(String username, String password, String telephone) {
//
//        User user = userRepository.findUserByUsername(username);
//        if (user == null) {
//            user = new User();
//            user.setUsername(username);
//            userRepository.save(user);
//        }
//        return user;
//    }
    public Role createRoleIfNotFound(String name, Set<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
