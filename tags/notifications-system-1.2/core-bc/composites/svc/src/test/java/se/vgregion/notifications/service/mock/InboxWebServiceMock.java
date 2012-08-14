/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.notifications.service.mock;

import se.vgregion.portal.raindancenotifier.ws.domain.*;

import java.util.ArrayList;
import java.util.List;

public class InboxWebServiceMock implements InboxNotifier {

    private List<InboxResponse> invoices = new ArrayList<InboxResponse>();
    private UserInformation userCredentials;
    // user credentials that is provided as method parameter.
    private UserInformation prividedUserInformation;

    // InboxConfigurationObject that is provided as method parameter.

    public InboxWebServiceMock(String userId, String password, String unit) {
        userCredentials = new UserInformation();
        userCredentials.setUserId(userId);
        userCredentials.setPassword(password);
    }

    public List<InboxResponse> getInvoices() {
        return invoices;
    }

    public UserInformation getPrividedUserInformation() {
        return prividedUserInformation;
    }

    public void setInvoices(List<InboxResponse> invoices) {
        this.invoices = invoices;
    }

    public void setUserInformation(UserInformation userCredentials) {
        this.userCredentials = userCredentials;
    }

    private boolean checkCredentials(UserInformation credentials) {
        boolean isValid = false;
        if (userCredentials.getUserId().equals(credentials.getUserId())) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    public List<InboxResponse> getProjectsCosts(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxSizeResponse> getNumberOfInvoices(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxResponse> getInvoices(RDConfiguration config, UserInformation usercredentials) {
        // Save provided usercredentials.
        this.prividedUserInformation = usercredentials;
        List<InboxResponse> invoices = null;
        if (checkCredentials(usercredentials)) {
            invoices = this.invoices;
        }
        return invoices;
    }

    @Override
    public String testConnection(String inStr) {
        return getClass().getSimpleName() + " replies " + inStr;
    }

    @Override
    public List<InboxResponse> getSubscriptions(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxSizeResponse> getNumberOfSubscriptions(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxResponse> getVouchers(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxSizeResponse> getNumberOfVouchers(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

    @Override
    public List<InboxSizeResponse> getNumberOfProjectsCosts(RDConfiguration config, UserInformation userInformation) {
        throw new UnsupportedOperationException("TODO: Implement this method");
    }

}