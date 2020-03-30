package com.study.spring.user.service;

import com.study.spring.user.dao.UserDao;
import com.study.spring.user.domain.Level;
import com.study.spring.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.study.spring.user.service.DefaultUserLevelUpgradePolicy.MIN_LOG_COUNT_FOR_SILVER;
import static com.study.spring.user.service.DefaultUserLevelUpgradePolicy.MIN_RECOMMEND_FOR_GOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "../../../../../../resources/spring/applicationContext-test.xml")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MailSender mailSender;

    private List<User> userList;

    @BeforeEach
    public void setUp() {
        userList = Arrays.asList(
                new User("user1", "username1", "password1", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER - 1, 0, "vvshinevv@naver.com"),
                new User("user2", "username2", "password2", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER, 0, "vvshinevv@naver.com"),
                new User("user3", "username3", "password3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1, "vvshinevv@naver.com"),
                new User("user4", "username4", "password4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "vvshinevv@naver.com"),
                new User("user5", "username5", "password5", Level.GOLD, 100, Integer.MAX_VALUE, "vvshinevv@naver.com")
        );
    }

    @Test
    public void bean() {
        assertNotNull(this.userService);
    }

    @DisplayName("레벨 업그레이드 하는 부분")
    @Test
    public void upgradeLevels() {
        userDao.deleteAll();

        for (User user : this.userList) {
            userDao.add(user);
        }

        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(userList.get(0), false);
        checkLevelUpgraded(userList.get(1), true);
        checkLevelUpgraded(userList.get(2), false);
        checkLevelUpgraded(userList.get(3), true);
        checkLevelUpgraded(userList.get(4), false);

        List<String> requestList = mockMailSender.getRequestList();
        assertEquals(requestList.size(), 2);
        assertEquals(requestList.get(0), userList.get(1).getEmail());
        assertEquals(requestList.get(1), userList.get(3).getEmail());
    }

    @DisplayName("add() 메소드의 테스트")
    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = userList.get(4);
        User userWithoutLevel = userList.get(0);
        userWithLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertEquals(userWithLevelRead.getLevel(), userWithLevel.getLevel());
        assertEquals(userWithoutLevelRead.getLevel(), Level.BASIC);
    }

    @DisplayName("예외 발생 시 작업 취소 여부 테스트")
    @Test
    public void upgradeAllOrNothingWithException() {

        UserLevelUpgradePolicy userLevelUpgradePolicy = new TestUserLevelUpgradePolicy(userList.get(3).getId());
        UserService userService = new UserService();

        userService.setUserDao(this.userDao);
        userService.setTransactionManager(this.transactionManager);
        userService.setUserLevelUpgradePolicy(userLevelUpgradePolicy);
        userService.setMailSender(this.mailSender);

        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }

        assertThrows(TestUserServiceException.class, userService::upgradeLevels);
        checkLevelUpgraded(userList.get(1), false);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertEquals(userUpdate.getLevel(), user.getLevel().nextLevel());
        } else {
            assertEquals(userUpdate.getLevel(), user.getLevel());
        }
    }

    static class TestUserLevelUpgradePolicy extends DefaultUserLevelUpgradePolicy {

        private String id;

        public TestUserLevelUpgradePolicy(String id) {
            this.id = id;
        }

        @Override
        public void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) {
                throw new TestUserServiceException();
            }

            user.upgradeLevel();
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }

    static class MockMailSender implements MailSender {
        private List<String> requestList = new ArrayList<>();

        public List<String> getRequestList() {
            return requestList;
        }

        @Override
        public void send(SimpleMailMessage simpleMailMessage) throws MailException {
            requestList.add(Objects.requireNonNull(simpleMailMessage.getTo())[0]);
        }

        @Override
        public void send(SimpleMailMessage... simpleMailMessages) throws MailException {

        }

    }
}


