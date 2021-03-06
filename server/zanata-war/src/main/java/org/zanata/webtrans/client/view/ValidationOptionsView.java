package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationOptionsView extends Composite implements ValidationOptionsPresenter.Display
{

   private static ValidationOptionsViewUiBinder uiBinder = GWT.create(ValidationOptionsViewUiBinder.class);

   interface ValidationOptionsViewUiBinder extends UiBinder<Widget, ValidationOptionsView>
   {
   }

   @UiField
   Label header;

   @UiField
   VerticalPanel contentPanel;

   public ValidationOptionsView()
   {
      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public HasValueChangeHandlers<Boolean> addValidationSelector(String label, String tooltip, boolean enabled)
   {
      CheckBox chk = new CheckBox(label);
      chk.setValue(enabled);
      chk.setTitle(tooltip);
      contentPanel.add(chk);

      return chk;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

}
