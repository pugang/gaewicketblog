package org.gaewicketblog.wicket.page;

import java.text.SimpleDateFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.gaewicketblog.common.AppEngineHelper;
import org.gaewicketblog.common.WicketHelper;
import org.gaewicketblog.model.Comment;
import org.gaewicketblog.model.CommentHelper;
import org.gaewicketblog.model.TopicSetting;
import org.gaewicketblog.model.TopicSettingHelper;
import org.gaewicketblog.wicket.application.BlogApplication;
import org.gaewicketblog.wicket.common.SimplePagingNavigator;
import org.gaewicketblog.wicket.panel.RecentPostsPanel;
import org.gaewicketblog.wicket.panel.StatusDescriptionPanel;
import org.gaewicketblog.wicket.provider.DatabaseCommentProvider;
import org.gaewicketblog.wicket.provider.ICommentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class IssueListPage extends BorderPage {

	private final static Logger log = LoggerFactory.getLogger(IssueListPage.class);

	private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd-MMM-yy");

	public IssueListPage() {
		super();
		String path = WicketHelper.getCurrentRestfulPath();
		BlogApplication app = (BlogApplication) getApplication();
		TopicSetting setting = TopicSettingHelper.getByPath(app.topics, path);
		DatabaseCommentProvider provider = new DatabaseCommentProvider(setting.id);
		init(setting, provider);
	}

//	public IssueListPage(long parentid) {
//		super();
//		log.debug("parentid="+parentid);
//		DatabaseCommentProvider provider = new DatabaseCommentProvider(parentid);
//		BlogApplication app = (BlogApplication) getApplication();
//		TopicSetting setting = TopicSettingHelper.getById(app.topics, parentid);
//		init(setting, provider);
//	}

	/**
	 * To display arbitrary comments, e.g. from search.
	 * @param comments
	 */
//	public IssueListPage(TopicSetting setting, List<Comment> comments){
//		super();
//		FixedCommentProvider provider = new FixedCommentProvider(comments);
//		init(setting, provider);
//	}

	private void init(final TopicSetting setting, SortableDataProvider<Comment> provider) {
		log.debug("ListPage<long> "+setting);

		add(new Label("topic", setting.topic));
		add(new Link<Void>("topicdescaddlink"){
			@Override
			public void onClick() {
				setResponsePage(new AddPage(setting, getString("issuelistpage.post")));
			}
		}.add(new Label("topicdesc", setting.topicdesc)));

		String adminemail = getString("admin.email");
		boolean admin = AppEngineHelper.isCurrentUser(adminemail);

		final DataView<Comment> dataView = new DataView<Comment>("sorting", provider) {
			@Override
			protected void populateItem(final Item<Comment> item) {
				final Comment comment = item.getModelObject();
				Integer status = comment.getStatus();
				item.add(CommentHelper.newStatusColorLabel(this, "status",
						status));
				item.add(new Label("votes", ""+comment.getVotes()));
				item.add(new Label("author", comment.getAuthor()));
				item.add(new Label("date", DATEFORMAT.format(comment.getDate())));
				item.add(new Label("comments", ""+comment.getComments()));
				item.add(new ExternalLink("viewpost", CommentHelper
						.getUrlPath(comment)).add(new Label("subject", comment
						.getSubject())));
				item.add(new AttributeModifier("class", true,
						new AbstractReadOnlyModel<String>() {
							@Override
							public String getObject() {
								return (item.getIndex() % 2 == 1) ? "even"
										: "odd";
							}
						}));
			}
		};

		dataView.setItemsPerPage(15);
		provider.setSort(ICommentProvider.SORT_DATE, false);

		add(newOrderByBorder("orderByStatus", ICommentProvider.SORT_STATUS,
				provider, dataView));
		add(newOrderByBorder("orderBySubject", ICommentProvider.SORT_SUBJECT,
				provider, dataView));
		add(newOrderByBorder("orderByAuthor", ICommentProvider.SORT_AUTHOR,
				provider, dataView));
		add(newOrderByBorder("orderByDate", ICommentProvider.SORT_DATE,
				provider, dataView));
		add(newOrderByBorder("orderByVotes", ICommentProvider.SORT_VOTES,
				provider, dataView));
		add(newOrderByBorder("orderByComments", ICommentProvider.SORT_COMMENTS,
				provider, dataView));

		add(dataView);

		add(new SimplePagingNavigator("navigator", dataView));

		//add
		add(new Link<String>("add") {
			@Override
			public void onClick() {
				setResponsePage(new AddPage(setting, getString("issuelistpage.post")));
			}
		}.setVisible(setting.canPost || admin));

		addSidebarPanel(new RecentPostsPanel(nextSidebarId(), provider));
		addSidebarPanel(new StatusDescriptionPanel(nextSidebarId()));
	}

	private static OrderByBorder newOrderByBorder(String id, String sort,
			SortableDataProvider<Comment> provider,
			final DataView<Comment> dataView) {
		OrderByBorder order = new OrderByBorder(id, sort, provider) {
			@Override
			protected void onSortChanged() {
				dataView.setCurrentPage(0);
			}
		};
		order.setVersioned(false);
		return order;
	}

}
