$path = "f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"
$content = [System.IO.File]::ReadAllText($path)

# ===== Change 1: safeNotifyChannelAdapter - remove channelRecyclerView null check for API 19 =====
$old1 = "    private void safeNotifyChannelAdapter() {
        if (channelItemAdapter == null || channelRecyclerView == null) return;
        if (Build.VERSION.SDK_INT < 20) {
            Log.d(TAG, ""safeNotify API19 mode: manualRender"");
            // API 19: 绕过 RecyclerView 适配器，直接手动构建视图
            channelItemAdapter.setItems(getCurrentGroupChannels());
            manualRenderChannelList();
        } else {
            channelItemAdapter.updateData(getCurrentGroupChannels());
            channelItemAdapter.notifyDataSetChanged();
        }
    }"

$new1 = "    private void safeNotifyChannelAdapter() {
        if (channelItemAdapter == null) return;
        if (Build.VERSION.SDK_INT < 20) {
            Log.d(TAG, ""safeNotify API19 mode: manualRender"");
            channelItemAdapter.setItems(getCurrentGroupChannels());
            manualRenderChannelList();
        } else {
            if (channelRecyclerView == null) return;
            channelItemAdapter.updateData(getCurrentGroupChannels());
            channelItemAdapter.notifyDataSetChanged();
        }
    }"

$content = $content.Replace($old1, $new1)

# ===== Change 2: forceRebindAllVisibleItems - skip for API 19 =====
$old2 = "    @android.annotation.TargetApi(19)
    private void forceRebindAllVisibleItems() {
        if (channelRecyclerView == null || channelItemAdapter == null) return;
        channelRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelRecyclerView.getChildCount();
                if (childCount == 0) {
                    // API 19 RecyclerView 尚未完成首次布局，直接强制整个列表刷新
                    channelRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            channelItemAdapter.notifyDataSetChanged();
                            channelRecyclerView.requestLayout();
                            channelRecyclerView.invalidate();
                        }
                    });
                    return;
                }
                for (int i = 0; i < childCount; i++) {
                    View child = channelRecyclerView.getChildAt(i);
                    int pos = channelRecyclerView.getChildAdapterPosition(child);
                    if (pos >= 0) {
                        channelItemAdapter.notifyItemChanged(pos);
                    }
                }
                // 二次刷新确保所有视图重绘
                channelRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int childCount2 = channelRecyclerView.getChildCount();
                        for (int i = 0; i < childCount2; i++) {
                            View child = channelRecyclerView.getChildAt(i);
                            int pos = channelRecyclerView.getChildAdapterPosition(child);
                            if (pos >= 0) {
                                channelItemAdapter.notifyItemChanged(pos);
                            }
                        }
                        // 强制 RecyclerView 重新布局
                        channelRecyclerView.requestLayout();
                        channelRecyclerView.invalidate();
                    }
                });
            }
        });
    }"

$new2 = "    @android.annotation.TargetApi(19)
    private void forceRebindAllVisibleItems() {
        // API 19: 使用 ScrollView+LinearLayout，不需要强制刷新
        if (Build.VERSION.SDK_INT < 20) return;
        if (channelRecyclerView == null || channelItemAdapter == null) return;
        channelRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelRecyclerView.getChildCount();
                if (childCount == 0) {
                    channelRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            channelItemAdapter.notifyDataSetChanged();
                            channelRecyclerView.requestLayout();
                            channelRecyclerView.invalidate();
                        }
                    });
                    return;
                }
                for (int i = 0; i < childCount; i++) {
                    View child = channelRecyclerView.getChildAt(i);
                    int pos = channelRecyclerView.getChildAdapterPosition(child);
                    if (pos >= 0) {
                        channelItemAdapter.notifyItemChanged(pos);
                    }
                }
                channelRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int childCount2 = channelRecyclerView.getChildCount();
                        for (int i = 0; i < childCount2; i++) {
                            View child = channelRecyclerView.getChildAt(i);
                            int pos = channelRecyclerView.getChildAdapterPosition(child);
                            if (pos >= 0) {
                                channelItemAdapter.notifyItemChanged(pos);
                            }
                        }
                        channelRecyclerView.requestLayout();
                        channelRecyclerView.invalidate();
                    }
                });
            }
        });
    }"

$content = $content.Replace($old2, $new2)

# ===== Change 3: manualRenderChannelList - use channelListInnerLayout =====
$old3 = "    private void manualRenderChannelList() {
        if (channelRecyclerView == null || channelItemAdapter == null) return;
        // 仅在菜单可见时才重建视图（避免后台重建大量View浪费内存）
        if (menuPanel.getVisibility() != View.VISIBLE) return;
        // 记录列表可见区域顶部位置
        int firstVisiblePos = 0;
        View firstChild = channelRecyclerView.getChildCount() > 0 ? channelRecyclerView.getChildAt(0) : null;
        if (firstChild != null) {
            firstVisiblePos = channelRecyclerView.getChildAdapterPosition(firstChild);
            if (firstVisiblePos < 0) firstVisiblePos = 0;
        }
        // 移除所有子视图
        channelRecyclerView.removeAllViews();
        List<Channel> items = channelItemAdapter.getItems();
        if (items == null || items.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            Channel ch = items.get(i);
            LinearLayout container = channelItemAdapter.createItemView(ch, i);
            channelRecyclerView.addView(container);
        }
        // 尝试保持滚动位置
        final int scrollTarget = firstVisiblePos;
        channelRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelRecyclerView.getChildCount();
                if (childCount > scrollTarget) {
                    View target = channelRecyclerView.getChildAt(Math.min(scrollTarget, childCount - 1));
                    if (target != null) {
                        target.requestFocus();
                        int top = target.getTop();
                        channelRecyclerView.scrollTo(0, Math.max(0, top - dp(4)));
                    }
                }
                channelRecyclerView.requestLayout();
                channelRecyclerView.invalidate();
            }
        });
    }"

$new3 = "    private void manualRenderChannelList() {
        if (channelItemAdapter == null) return;
        if (channelListInnerLayout == null) return;
        // 仅在菜单可见时才重建视图（避免后台重建大量View浪费内存）
        if (menuPanel.getVisibility() != View.VISIBLE) return;
        // 记录列表可见区域顶部位置
        int firstVisiblePos = 0;
        View firstChild = channelListInnerLayout.getChildCount() > 0 ? channelListInnerLayout.getChildAt(0) : null;
        if (firstChild != null) {
            firstVisiblePos = (int) firstChild.getTag(0x1001);  // 存储位置的自定义tag
            if (firstVisiblePos < 0) firstVisiblePos = 0;
        }
        // 移除所有子视图
        channelListInnerLayout.removeAllViews();
        List<Channel> items = channelItemAdapter.getItems();
        if (items == null || items.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            Channel ch = items.get(i);
            LinearLayout container = channelItemAdapter.createItemView(ch, i);
            container.setTag(0x1001, i);  // 存储位置信息
            channelListInnerLayout.addView(container);
        }
        // 尝试保持滚动位置
        final int scrollTarget = firstVisiblePos;
        channelListContainer.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelListInnerLayout.getChildCount();
                if (childCount > scrollTarget) {
                    View target = channelListInnerLayout.getChildAt(Math.min(scrollTarget, childCount - 1));
                    if (target != null) {
                        target.requestFocus();
                        int top = target.getTop();
                        channelListContainer.scrollTo(0, Math.max(0, top - dp(4)));
                    }
                }
                channelListContainer.requestLayout();
                channelListContainer.invalidate();
            }
        });
    }"

$content = $content.Replace($old3, $new3)

# ===== Change 4: scrollChannelToPosition - use channelListContainer for API 19 =====
$old4 = "    private void scrollChannelToPosition(int position) {
        if (channelRecyclerView == null) return;
        if (position < 0) position = 0;
        int total = channelItemAdapter.getItemCount();
        if (position >= total) position = Math.max(0, total - 1);
        if (Build.VERSION.SDK_INT < 20) {
            // API 19: 手动渲染模式下，用 requestFocus + scrollTo
            final int targetPos = position;
            channelRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    int childCount = channelRecyclerView.getChildCount();
                    if (childCount > 0 && targetPos < childCount) {
                        View target = channelRecyclerView.getChildAt(targetPos);
                        if (target != null) {
                            int top = target.getTop();
                            channelRecyclerView.scrollTo(0, Math.max(0, top - dp(4)));
                            target.requestFocus();
                        }
                    }
                }
            });
        } else {
            if (channelRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager) channelRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, dp(4));
            } else {
                channelRecyclerView.scrollToPosition(position);
            }
        }
    }"

$new4 = "    private void scrollChannelToPosition(int position) {
        if (Build.VERSION.SDK_INT < 20) {
            if (channelListContainer == null || channelListInnerLayout == null) return;
            if (position < 0) position = 0;
            List<Channel> items = channelItemAdapter.getItems();
            int total = items != null ? items.size() : 0;
            if (position >= total) position = Math.max(0, total - 1);
            // API 19: 用 scrollTo 滚动 ScrollView
            final int targetPos = position;
            channelListContainer.post(new Runnable() {
                @Override
                public void run() {
                    int childCount = channelListInnerLayout.getChildCount();
                    if (childCount > 0 && targetPos < childCount) {
                        View target = channelListInnerLayout.getChildAt(targetPos);
                        if (target != null) {
                            int top = target.getTop();
                            channelListContainer.scrollTo(0, Math.max(0, top - dp(4)));
                            target.requestFocus();
                        }
                    }
                }
            });
        } else {
            if (channelRecyclerView == null) return;
            if (position < 0) position = 0;
            int total = channelItemAdapter.getItemCount();
            if (position >= total) position = Math.max(0, total - 1);
            if (channelRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager) channelRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, dp(4));
            } else {
                channelRecyclerView.scrollToPosition(position);
            }
        }
    }"

$content = $content.Replace($old4, $new4)

# ===== Change 5: showGroupsMenu - change channelRecyclerView.requestFocus() for API 19 =====
# showGroupChannelsMenu uses channelRecyclerView.requestFocus()
$old5 = "    private void showGroupChannelsMenu() {
        if (menuGroups.size() > 0 && selectedGroupIndex >= menuGroups.size()) {
            selectedGroupIndex = 0;
        }
        menuPage = MENU_PAGE_GROUP_CHANNELS;
        focusOnGroupSide = false;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        groupAdapter.notifyDataSetChanged();
        safeNotifyChannelAdapter();
        scrollGroupToPosition(selectedGroupIndex);
        scrollChannelToPosition(selectedChannelInGroup);
        channelRecyclerView.requestFocus();
        resetMenuAutoHide();
    }"

$new5 = "    private void showGroupChannelsMenu() {
        if (menuGroups.size() > 0 && selectedGroupIndex >= menuGroups.size()) {
            selectedGroupIndex = 0;
        }
        menuPage = MENU_PAGE_GROUP_CHANNELS;
        focusOnGroupSide = false;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        groupAdapter.notifyDataSetChanged();
        safeNotifyChannelAdapter();
        scrollGroupToPosition(selectedGroupIndex);
        scrollChannelToPosition(selectedChannelInGroup);
        if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
            channelListContainer.requestFocus();
        } else if (channelRecyclerView != null) {
            channelRecyclerView.requestFocus();
        }
        resetMenuAutoHide();
    }"

$content = $content.Replace($old5, $new5)

# ===== Change 6: onGroupItemClicked - channelRecyclerView.requestFocus() for API 19 =====
$old6 = "    private void onGroupItemClicked(int position) {
        Log.d(TAG, ""onGroupItemClicked pos="" + position + "" totalGroups="" + menuGroups.size());
        if (position < menuGroups.size()) {
            onGroupSelected(position);
            focusOnGroupSide = false;
            channelRecyclerView.requestFocus();
        } else if (position == menuGroups.size()) {
            showSettingsMenu();
        } else if (position == menuGroups.size() + 1) {
            hideMenu();
        }
    }"

$new6 = "    private void onGroupItemClicked(int position) {
        Log.d(TAG, ""onGroupItemClicked pos="" + position + "" totalGroups="" + menuGroups.size());
        if (position < menuGroups.size()) {
            onGroupSelected(position);
            focusOnGroupSide = false;
            if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
                channelListContainer.requestFocus();
            } else if (channelRecyclerView != null) {
                channelRecyclerView.requestFocus();
            }
        } else if (position == menuGroups.size()) {
            showSettingsMenu();
        } else if (position == menuGroups.size() + 1) {
            hideMenu();
        }
    }"

$content = $content.Replace($old6, $new6)

# ===== Change 7: dispatchKeyEvent - channelRecyclerView.requestFocus() for API 19 =====
$old7 = "            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (focusOnGroupSide) {
                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        channelRecyclerView.requestFocus();
                        refreshMenuAdapters();
                    } else {
                        onGroupItemClicked(selectedGroupIndex);
                    }
                }
                return true;
            }"

$new7 = "            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (focusOnGroupSide) {
                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        View focusTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                        if (focusTarget != null) focusTarget.requestFocus();
                        refreshMenuAdapters();
                    } else {
                        onGroupItemClicked(selectedGroupIndex);
                    }
                }
                return true;
            }"

$content = $content.Replace($old7, $new7)

# ===== Change 8: handleTouchSwipe - channelRecyclerView.requestFocus() for API 19 =====
$old8 = "            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx < 0) {
                    focusOnGroupSide = false;
                    channelRecyclerView.requestFocus();
                    refreshMenuAdapters();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                    refreshMenuAdapters();
                }
            }"

$new8 = "            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx < 0) {
                    focusOnGroupSide = false;
                    View focusTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                    if (focusTarget != null) focusTarget.requestFocus();
                    refreshMenuAdapters();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                    refreshMenuAdapters();
                }
            }"

$content = $content.Replace($old8, $new8)

# ===== Change 9: GestureTraceView onTouchEvent - channelRecyclerView.scrollBy for API 19 =====
$old9 = "                            if (focusOnGroupSide && groupRecyclerView != null) {
                                groupRecyclerView.scrollBy(0, (int) -stepDy);
                            } else if (channelRecyclerView != null) {
                                channelRecyclerView.scrollBy(0, (int) -stepDy);
                            }"

$new9 = "                            if (focusOnGroupSide && groupRecyclerView != null) {
                                groupRecyclerView.scrollBy(0, (int) -stepDy);
                            } else if (channelRecyclerView != null || channelListContainer != null) {
                                View sv = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                                if (sv != null) sv.scrollBy(0, (int) -stepDy);
                            }"

$content = $content.Replace($old9, $new9)

# ===== Change 10: buildMenu - remove the old API 19 scroll listener that references dummy channelRecyclerView =====
$old10 = "        if (Build.VERSION.SDK_INT < 20) {
            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        forceRebindAllVisibleItems();
                    }
                }
            });
        }
        channelRecyclerView.setOnTouchListener(new View.OnTouchListener()"

$new10 = "        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView != null) {
            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        forceRebindAllVisibleItems();
                    }
                }
            });
        }
        View channelTouchTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
        channelTouchTarget.setOnTouchListener(new View.OnTouchListener()"

$content = $content.Replace($old10, $new10)

# ===== Change 11: detectBadChannels - fix channelItemAdapter.getItemCount() call after safeNotifyChannelAdapter =====
$old11 = "                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int cnt = channelItemAdapter.getItemCount();
                        safeNotifyChannelAdapter();
                        if (cnt > 0) {
                            channelItemAdapter.notifyItemRangeChanged(0, cnt);
                        }
                        groupAdapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });"

$new11 = "                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int cnt = channelItemAdapter.getItemCount();
                        safeNotifyChannelAdapter();
                        if (cnt > 0 && Build.VERSION.SDK_INT >= 20) {
                            channelItemAdapter.notifyItemRangeChanged(0, cnt);
                        }
                        groupAdapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });"

$content = $content.Replace($old11, $new11)

# ===== CHANGE 12: GestureTraceView - add ScrollView import =====
# Need to import android.widget.ScrollView - check if already imported
if ($content.Contains("import android.widget.ScrollView") -eq $false) {
    $content = $content.Replace("import android.widget.ProgressBar;", "import android.widget.ProgressBar;`r`nimport android.widget.ScrollView;")
}

[System.IO.File]::WriteAllText($path, $content)
Write-Host "DONE - All changes applied"