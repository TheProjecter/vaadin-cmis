package com.jensjansson.pagedtable;

import java.util.logging.Logger;

import com.fatminds.vaadin.cmis.CmisContainer;
import com.jensjansson.pagedtable.PagedTable.PageChangeListener;
import com.jensjansson.pagedtable.PagedTable.PagedTableChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;

public class FMPagedTable extends PagedTable{

	private static final Logger log =
	          Logger.getLogger(FMPagedTable.class.toString());
	  
	protected int hackPageLength=0;
	
	 @Override
	 public int getTotalAmountOfPages() {
		 
	        int size = ((PagedTableContainer)getContainerDataSource()).getRealSize();
	        double pageLength = getPageLength();
//	        log.info("Container " + getContainerDataSource() + " says its size is " + size + ", container pageLength is " 
//	        		+ ((PagedTableContainer)getContainerDataSource()).getPageLength() 
//	        		+ ", pagelength is " 
//	        		+ getPageLength());
	        int pageCount = (int) Math.ceil(size / pageLength);
	        if (pageCount < 1) {
	            pageCount = 1;
	        }
	        log.info("totalAmountOfPages = " + pageCount);
	        return pageCount;
	        
	    }
	 
	 
	 @Override
	 public void nextPage() {
//		 log.info("before nextPage(): pagelength = " + this.getPageLength());
		 super.nextPage();
//		 log.info("after nextPage(): pagelength = " + this.getPageLength());
	 }
	 
//	    @Override
//	    public void setPageLength(int pageLength) {
//	        if (pageLength >= 0 && getPageLength() != pageLength) {
//	        	log.info("Setting hackPageLength = " + pageLength);
//	            this.hackPageLength = pageLength;
//	            super.setPageLength(pageLength);
//	            super.setPageLength(pageLength);
//	        }
//	    }

	 @Override
	 public int getPageLength() {
//		 log.info(((Object)this).hashCode() + " Returning pageLength = " + super.getPageLength() );
		 return super.getPageLength();
	 }
	 
	 @Override
	    public HorizontalLayout createControls() {
	        Label itemsPerPageLabel = new Label("Items per page:");
	        Label totalItemsLabel = new Label();
	        totalItemsLabel.setValue("Total Items: " + ((PagedTableContainer)getContainerDataSource()).getRealSize());
	        totalItemsLabel.setWidth("100px");
	        final ComboBox itemsPerPageSelect = new ComboBox();

	        //itemsPerPageSelect.addItem("5");
	        itemsPerPageSelect.addItem("10");
	        itemsPerPageSelect.addItem("25");
	        itemsPerPageSelect.addItem("50");
	        itemsPerPageSelect.addItem("100");
	        itemsPerPageSelect.addItem("600");
	        itemsPerPageSelect.setImmediate(true);
	        itemsPerPageSelect.setNullSelectionAllowed(false);
	        itemsPerPageSelect.setWidth("50px");
	        itemsPerPageSelect.addListener(new ValueChangeListener() {
	            private static final long serialVersionUID = -2255853716069800092L;

	            public void valueChange(
	                    com.vaadin.data.Property.ValueChangeEvent event) {
	                setPageLength(Integer.valueOf(String.valueOf(event
	                        .getProperty().getValue())));
	            }
	        });
	        //itemsPerPageSelect(this.getContainerDataSource().getPageLength());
	        itemsPerPageSelect.select("10");
	        Label pageLabel = new Label("Page:&nbsp;", Label.CONTENT_XHTML);
	        final TextField currentPageTextField = new TextField();
	        currentPageTextField.setValue(String.valueOf(getCurrentPage()));
	        currentPageTextField.addValidator(new IntegerValidator(null));
	        Label separatorLabel = new Label("&nbsp;/&nbsp;", Label.CONTENT_XHTML);
	        final Label totalPagesLabel = new Label(
	                String.valueOf(getTotalAmountOfPages()), Label.CONTENT_XHTML);
	        currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
	        currentPageTextField.setImmediate(true);
	        currentPageTextField.addListener(new ValueChangeListener() {
	            private static final long serialVersionUID = -2255853716069800092L;

	            public void valueChange(
	                    com.vaadin.data.Property.ValueChangeEvent event) {
	                if (currentPageTextField.isValid()
	                        && currentPageTextField.getValue() != null) {
	                    int page = Integer.valueOf(String
	                            .valueOf(currentPageTextField.getValue()));
	                    setCurrentPage(page);
	                }
	            }
	        });
	        pageLabel.setWidth(null);
	        currentPageTextField.setWidth("20px");
	        separatorLabel.setWidth(null);
	        totalPagesLabel.setWidth(null);

	        HorizontalLayout controlBar = new HorizontalLayout();
	        HorizontalLayout pageSize = new HorizontalLayout();
	        HorizontalLayout pageManagement = new HorizontalLayout();
	        final Button first = new Button("<<", new ClickListener() {
	            private static final long serialVersionUID = -355520120491283992L;

	            public void buttonClick(ClickEvent event) {
	                setCurrentPage(0);
	            }
	        });
	        final Button previous = new Button("<", new ClickListener() {
	            private static final long serialVersionUID = -355520120491283992L;

	            public void buttonClick(ClickEvent event) {
	                previousPage();
	            }
	        });
	        final Button next = new Button(">", new ClickListener() {
	            private static final long serialVersionUID = -1927138212640638452L;

	            public void buttonClick(ClickEvent event) {
	                nextPage();
	            }
	        });
	        final Button last = new Button(">>", new ClickListener() {
	            private static final long serialVersionUID = -355520120491283992L;

	            public void buttonClick(ClickEvent event) {
	                setCurrentPage(getTotalAmountOfPages());
	            }
	        });
	        first.setStyleName(Reindeer.BUTTON_LINK);
	        previous.setStyleName(Reindeer.BUTTON_LINK);
	        next.setStyleName(Reindeer.BUTTON_LINK);
	        last.setStyleName(Reindeer.BUTTON_LINK);

	        itemsPerPageLabel.addStyleName("pagedtable-itemsperpagecaption");
	        itemsPerPageSelect.addStyleName("pagedtable-itemsperpagecombobox");
	        pageLabel.addStyleName("pagedtable-pagecaption");
	        currentPageTextField.addStyleName("pagedtable-pagefield");
	        separatorLabel.addStyleName("pagedtable-separator");
	        totalPagesLabel.addStyleName("pagedtable-total");
	        first.addStyleName("pagedtable-first");
	        previous.addStyleName("pagedtable-previous");
	        next.addStyleName("pagedtable-next");
	        last.addStyleName("pagedtable-last");

	        itemsPerPageLabel.addStyleName("pagedtable-label");
	        itemsPerPageSelect.addStyleName("pagedtable-combobox");
	        pageLabel.addStyleName("pagedtable-label");
	        currentPageTextField.addStyleName("pagedtable-label");
	        separatorLabel.addStyleName("pagedtable-label");
	        totalPagesLabel.addStyleName("pagedtable-label");
	        first.addStyleName("pagedtable-button");
	        previous.addStyleName("pagedtable-button");
	        next.addStyleName("pagedtable-button");
	        last.addStyleName("pagedtable-button");

	        pageSize.addComponent(itemsPerPageLabel);
	        pageSize.addComponent(itemsPerPageSelect);
	        pageSize.addComponent(totalItemsLabel);

	        pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
	        pageSize.setComponentAlignment(itemsPerPageSelect,
	                Alignment.MIDDLE_LEFT);
	        pageSize.setComponentAlignment(totalItemsLabel,
	                Alignment.MIDDLE_LEFT);
	        pageSize.setSpacing(true);
	        pageManagement.addComponent(first);
	        pageManagement.addComponent(previous);
	        pageManagement.addComponent(pageLabel);
	        pageManagement.addComponent(currentPageTextField);
	        pageManagement.addComponent(separatorLabel);
	        pageManagement.addComponent(totalPagesLabel);
	        pageManagement.addComponent(next);
	        pageManagement.addComponent(last);
	        pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(currentPageTextField,
	                Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(separatorLabel,
	                Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(totalPagesLabel,
	                Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
	        pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
	        pageManagement.setWidth(null);
	        pageManagement.setSpacing(true);
	        controlBar.addComponent(pageSize);
	        controlBar.addComponent(pageManagement);
	        controlBar.setComponentAlignment(pageManagement,
	                Alignment.MIDDLE_CENTER);
	        controlBar.setWidth("100%");
	        controlBar.setExpandRatio(pageSize, 1);
	        addListener(new PageChangeListener() {
	            public void pageChanged(PagedTableChangeEvent event) {
	            	PagedTableContainer container = (PagedTableContainer) event.table.getContainerDataSource();
	                first.setEnabled(container.getStartIndex() > 0);
	                previous.setEnabled(container.getStartIndex() > 0);
	                next.setEnabled(container.getStartIndex() < container
	                        .getRealSize() - getPageLength());
	                last.setEnabled(container.getStartIndex() < container
	                        .getRealSize() - getPageLength());
	                currentPageTextField.setValue(String.valueOf(getCurrentPage()));
	                totalPagesLabel.setValue(getTotalAmountOfPages());
	                itemsPerPageSelect.setValue(String.valueOf(getPageLength()));
	            }
	        });
	        return controlBar;
	    }


}
