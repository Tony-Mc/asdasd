package api.business.services;

import api.business.entities.Configuration;
import api.business.entities.Login;
import api.business.entities.Role;
import api.business.entities.User;
import api.business.services.interfaces.IUserService;
import api.business.services.interfaces.notifications.INotificationsService;
import api.contracts.enums.NotificationAction;
import clients.facebook.responses.FacebookUserDetails;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Stateless
public class UserService implements IUserService {
    @PersistenceContext
    private EntityManager em;
    @Inject
    private INotificationsService notificationsService;

    private final String welcomeNotification = "Welcome! You must receive %s recommendations to become a club member.";

    public User get(int id) {
        try {
            return em.createQuery("SELECT U FROM User U WHERE U.id = :id AND U.login.disabled = false", User.class)
                    .setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public User get() {
        Subject sub = SecurityUtils.getSubject();
        if (sub.isAuthenticated())
            return get(Integer.parseInt(sub.getPrincipal().toString()));
        return null;
    }

    public User getByEmail(String email) {
        try {
            TypedQuery<User> users = em.createQuery("SELECT U FROM User U WHERE U.login.email = :email", User.class)
                    .setParameter("email", email);
            return users.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void createUser(User user, Login login) {
        try {
            em.persist(user);
            em.persist(login);
            Role lr = new Role();
            lr.setRoleName("potentialCandidate");
            lr.setUsername(user.getLogin().getEmail());

            Configuration c = em.find(Configuration.class, "min_recommendation_required");
            notificationsService.create(String.format(welcomeNotification, c == null ? "2" : c.getValue()), NotificationAction.PROFILE, user.getId(), null);

            em.persist(lr);
            em.flush();
        } catch (Exception e) {
            em.clear();
            throw e;
        }
    }

    public void createFacebookUser(FacebookUserDetails details) {
        User user = new User();
        Login login = new Login();

        user.setName(details.Name);
        login.setFacebookId(details.Id);
        user.setPicture(details.Picture.getUrl());
        user.setLogin(login);

        login.setEmail(details.Email);

        PasswordService passwordService = new DefaultPasswordService();
        String encryptedPassword = passwordService.encryptPassword(UUID.randomUUID().toString());

        login.setPassword(encryptedPassword);

        createUser(user, login);
    }

    @Override
    public User getByFacebookId(String id) {
        try {
            TypedQuery<User> users = em.createQuery("SELECT U FROM User U WHERE U.login.facebookId = :id", User.class).setParameter("id", id);
            return users.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(User user) {
        try {
            em.merge(user);
            em.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void disableUser(int id) {
        get(id).getLogin().setDisabled(true);
    }

    public void disableUser() {
        get().getLogin().setDisabled(true);
    }

    public void logoutUser(int userId) {
        DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        DefaultSessionManager sessionManager = (DefaultSessionManager) securityManager.getSessionManager();
        Collection<Session> activeSessions = sessionManager.getSessionDAO().getActiveSessions();
        List<Session> sessions = activeSessions.stream().filter(s -> matchSession(s, userId)).collect(Collectors.toList());
        sessions.forEach(Session::stop);
    }

    private boolean matchSession(Session s, int userId) {
        Subject subject = new Subject.Builder().sessionId(s.getId()).buildSubject();
        Object principal = subject.getPrincipal();
        int id = Integer.parseInt(principal.toString());
        return id == userId;
    }
}
