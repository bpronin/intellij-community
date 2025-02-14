// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.diff.actions.impl;

import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.DiffRequestProducer;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.DiffBundle;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public final class GoToChangePopupBuilder {
  private static final Key<JBPopup> POPUP_KEY = Key.create("Diff.RequestChainGoToPopup");

  public interface Chain extends DiffRequestChain {
    @Nullable
    AnAction createGoToChangeAction(@NotNull Consumer<? super Integer> onSelected, int defaultSelection);
  }

  @NotNull
  public static AnAction create(@NotNull DiffRequestChain chain, @NotNull Consumer<? super Integer> onSelected, int defaultSelection) {
    if (chain instanceof Chain) {
      AnAction action = ((Chain)chain).createGoToChangeAction(onSelected, defaultSelection);
      if (action != null) return action;
    }
    return new SimpleGoToChangePopupAction(chain, onSelected, defaultSelection);
  }

  public static abstract class BaseGoToChangePopupAction extends GoToChangePopupAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      e.getPresentation().setEnabledAndVisible(canNavigate() && e.getData(DiffDataKeys.DIFF_CONTEXT) != null);
    }

    protected abstract boolean canNavigate();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      UserDataHolder context = e.getRequiredData(DiffDataKeys.DIFF_CONTEXT);

      JBPopup oldPopup = context.getUserData(POPUP_KEY);
      if (oldPopup != null && oldPopup.isVisible()) {
        oldPopup.cancel();
      }

      final JBPopup popup = createPopup(e);

      context.putUserData(POPUP_KEY, popup);
      popup.addListener(new JBPopupListener() {
        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
          if (context.getUserData(POPUP_KEY) == popup) {
            context.putUserData(POPUP_KEY, null);
          }
        }
      });

      InputEvent event = e.getInputEvent();
      if (event instanceof MouseEvent) {
        popup.show(new RelativePoint((MouseEvent)event));
      }
      else {
        popup.showInBestPositionFor(e.getDataContext());
      }
    }

    @NotNull
    protected abstract JBPopup createPopup(@NotNull AnActionEvent e);
  }

  private static class SimpleGoToChangePopupAction extends BaseGoToChangePopupAction {
    private final DiffRequestChain myChain;
    private final @NotNull Consumer<? super Integer> myOnSelected;
    private final int myDefaultSelection;

    SimpleGoToChangePopupAction(@NotNull DiffRequestChain chain, @NotNull Consumer<? super Integer> onSelected, int defaultSelection) {
      myChain = chain;
      myOnSelected = onSelected;
      myDefaultSelection = defaultSelection;
    }

    @Override
    protected boolean canNavigate() {
      return myChain.getRequests().size() > 1;
    }

    @NotNull
    @Override
    protected JBPopup createPopup(@NotNull AnActionEvent e) {
      return JBPopupFactory.getInstance().createListPopup(new MyListPopupStep());
    }

    private class MyListPopupStep extends BaseListPopupStep<DiffRequestProducer> {
      MyListPopupStep() {
        super(DiffBundle.message("action.presentation.go.to.change.text"), myChain.getRequests());
        setDefaultOptionIndex(myDefaultSelection);
      }

      @NotNull
      @Override
      public String getTextFor(DiffRequestProducer value) {
        return value.getName();
      }

      @Override
      public boolean isSpeedSearchEnabled() {
        return true;
      }

      @Override
      public PopupStep<?> onChosen(final DiffRequestProducer selectedValue, boolean finalChoice) {
        return doFinalStep(() -> {
          int index = myChain.getRequests().indexOf(selectedValue);
          myOnSelected.consume(index);
        });
      }
    }
  }
}
