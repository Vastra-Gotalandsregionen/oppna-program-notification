package se.vgregion.notifications.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.social.model.SocialRelationConstants;
import com.liferay.portlet.social.model.SocialRequest;
import com.liferay.portlet.social.model.SocialRequestConstants;
import com.liferay.portlet.social.service.SocialRelationLocalService;
import com.liferay.portlet.social.service.SocialRelationLocalServiceUtil;
import com.liferay.portlet.social.service.SocialRequestLocalService;
import com.liferay.portlet.social.service.SocialRequestLocalServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing social relations.
 *
 * @author Patrik Bergström
 */
@Service
public class SocialRelationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialRelationService.class);

    private SocialRelationLocalService socialRelationLocalService;
    private SocialRequestLocalService socialRequestLocalService;
    private UserLocalService userLocalService;

    /**
     * Default constructor taking no arguments. The dependencies are thus looked up by the constructor itself.
     */
    public SocialRelationService() {
        this.socialRelationLocalService = SocialRelationLocalServiceUtil.getService();
        this.socialRequestLocalService = SocialRequestLocalServiceUtil.getService();
        this.userLocalService = UserLocalServiceUtil.getService();
    }

    private int getUserRequestsCount(User user, int status) throws SystemException {
        return socialRequestLocalService.getReceiverUserRequestsCount(user.getUserId(), status);
    }

    /**
     * Get {@link SocialRequest}s where the given {@link User} is the user who is asked to establish a relation by the
     * requesting part.
     *
     * @param user         the requested {@link User}
     * @param cachedResult whether a cached result is acceptable
     * @return a list of {@link SocialRequest}s
     */
    // Note: the cachedResult parameter is handled by NotificationsCacheAspect
    public List<SocialRequest> getUserRequests(User user, boolean cachedResult) {
        try {
            List<SocialRequest> receiverUserRequests = socialRequestLocalService.getReceiverUserRequests(
                    user.getUserId(), SocialRequestConstants.STATUS_PENDING, 0, getUserRequestsCount(
                    user, SocialRequestConstants.STATUS_PENDING));
            return receiverUserRequests;
        } catch (SystemException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Like {@link SocialRelationService#getUserRequests(com.liferay.portal.model.User, boolean)} but where each
     * {@link SocialRequest} is mapped to a requesting {@link User} object for easy access to ditto.
     *
     * @param user         the requested {@link User}
     * @param cachedResult whether a cached result is acceptable
     * @return a map of {@link SocialRequest}s mapped to the requesting {@link User}
     */
    public Map<SocialRequest, User> getUserRequestsWithUser(User user, boolean cachedResult) {
        Map<SocialRequest, User> socialRequestUserMap = new HashMap<SocialRequest, User>();
        try {
            List<SocialRequest> receiverUserRequests = socialRequestLocalService.getReceiverUserRequests(
                    user.getUserId(), SocialRequestConstants.STATUS_PENDING, 0, getUserRequestsCount(
                    user, SocialRequestConstants.STATUS_PENDING));

            for (SocialRequest request : receiverUserRequests) {
                User requestingUser = userLocalService.getUserById(request.getUserId());
                socialRequestUserMap.put(request, requestingUser);
            }
        } catch (SystemException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } catch (PortalException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return socialRequestUserMap;
    }


    /**
     * Confirm a {@link SocialRequest} to establish a relation between the {@link User}s.
     *
     * @param requestId the id of the {@link SocialRequest}
     * @throws SystemException SystemException
     * @throws PortalException PortalException
     */
    public void confirmRequest(Long requestId) throws SystemException, PortalException {
        SocialRequest socialRequest = socialRequestLocalService.getSocialRequest(requestId);
        addFriendRelation(socialRequest.getUserId(), socialRequest.getReceiverUserId());

        socialRequest.setStatus(SocialRequestConstants.STATUS_CONFIRM);
        socialRequestLocalService.updateSocialRequest(socialRequest);
    }

    private void addFriendRelation(long userId, long receiverUserId) throws SystemException, PortalException {
        socialRelationLocalService.addRelation(userId, receiverUserId, SocialRelationConstants.TYPE_BI_FRIEND);
    }

    /**
     * Reject a {@link SocialRequest}.
     *
     * @param requestId the id of the {@link SocialRequest}
     * @throws SystemException SystemException
     * @throws PortalException PortalException
     */
    public void rejectRequest(Long requestId) throws SystemException, PortalException {
        SocialRequest socialRequest = socialRequestLocalService.getSocialRequest(requestId);
        socialRequest.setStatus(SocialRequestConstants.STATUS_IGNORE);
        socialRequestLocalService.updateSocialRequest(socialRequest);
    }
}
