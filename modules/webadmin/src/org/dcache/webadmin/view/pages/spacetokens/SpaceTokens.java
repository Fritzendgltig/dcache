package org.dcache.webadmin.view.pages.spacetokens;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.dcache.webadmin.controller.exceptions.LinkGroupsServiceException;
import org.dcache.webadmin.view.pages.AuthenticatedWebPage;
import org.dcache.webadmin.view.pages.basepage.BasePage;
import org.dcache.webadmin.view.pages.spacetokens.beans.LinkGroupBean;
import org.dcache.webadmin.view.pages.spacetokens.spacereservationpanel.SpaceReservationPanel;
import org.dcache.webadmin.view.util.EvenOddListView;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author jans
 */
public class SpaceTokens extends BasePage implements AuthenticatedWebPage {

    private Panel _spaceReservationsPanel =
            new EmptyPanel("spaceReservationsPanel");
    private List<LinkGroupBean> _linkGroups;
    private static final Logger _log = LoggerFactory.getLogger(SpaceTokens.class);

    public SpaceTokens() {
        createMarkup();
        getTokenInfo();
    }

    private void createMarkup() {
        add(new FeedbackPanel("feedback"));
        add(new LinkGroupListView("linkGroupView", new PropertyModel(this,
                "_linkGroups")));
        add(_spaceReservationsPanel);
    }

    private void getTokenInfo() {
        try {
            _linkGroups = getWebadminApplication().getLinkGroupsService().getLinkGroups();
        } catch (LinkGroupsServiceException ex) {
            this.error(getStringResource("error.getTokenInfoFailed") + ex.getMessage());
            _log.debug("getTokenInfo failed {}", ex.getMessage());
            _linkGroups = new ArrayList<LinkGroupBean>();
        }
    }

    private class LinkGroupListView extends EvenOddListView<LinkGroupBean> {

        LinkGroupListView(String id, IModel<? extends List<LinkGroupBean>> model) {
            super(id, model);
        }

        @Override
        protected void populateItem(ListItem<LinkGroupBean> item) {
            final LinkGroupBean linkGroup = item.getModelObject();
            Link linkGroupName = new Link("linkGroupLink") {

                @Override
                public void onClick() {
                    SpaceReservationPanel spaceReservations =
                            new SpaceReservationPanel("spaceReservationsPanel",
                            linkGroup.getName());
                    spaceReservations.setReservations(linkGroup.getReservations());
                    _spaceReservationsPanel.replaceWith(spaceReservations);
                    _spaceReservationsPanel = spaceReservations;
                }
            };
            linkGroupName.add(new Label("nameMessage", linkGroup.getName()));
            item.add(linkGroupName);
            item.add(new Label("id", linkGroup.getId()));
            item.add(new Label("allowed", linkGroup.getAllowed()));
            item.add(new Label("vo", linkGroup.getVos()));
            item.add(new Label("availableSpace", Long.valueOf(
                    linkGroup.getAvailable()).toString()));
            item.add(new Label("reservedSpace", Long.valueOf(
                    linkGroup.getReserved()).toString()));
            item.add(new Label("freeSpace", Long.valueOf(
                    linkGroup.getFree()).toString()));
            item.add(new Label("totalSpace", Long.valueOf(
                    linkGroup.getTotal()).toString()));
        }
    }
}
