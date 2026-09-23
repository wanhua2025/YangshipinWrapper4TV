$path = "f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"
$content = [System.IO.File]::ReadAllText($path)
$count = 0

# Change A: manualRenderChannelList - fix remaining channelRecyclerView refs
$old = "if (channelRecyclerView == null || channelItemAdapter == null) return;`r`n        // 仅在菜单可见时才重建视图"
$new = "if (channelItemAdapter == null) return;`r`n        if (channelListInnerLayout == null) return;`r`n        // 仅在菜单可见时才重建视图"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

$old = "channelRecyclerView.addView(container);"
$new = "channelListInnerLayout.addView(container);"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

$old = "channelRecyclerView.post(new Runnable() {`r`n            @Override`r`n            public void run() {`r`n                int childCount = channelListInnerLayout"
$new = "channelListContainer.post(new Runnable() {`r`n            @Override`r`n            public void run() {`r`n                int childCount = channelListInnerLayout"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change B: scrollChannelToPosition - fix for API 19
$old_scroll = "    private void scrollChannelToPosition(int position) {
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

$new_scroll = "    private void scrollChannelToPosition(int position) {
        if (Build.VERSION.SDK_INT < 20) {
            if (channelListContainer == null || channelListInnerLayout == null) return;
            if (position < 0) position = 0;
            int total = channelItemAdapter.getItemCount();
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
if ($content.Contains($old_scroll)) { $content = $content.Replace($old_scroll, $new_scroll); $count++ }

# Change C: showGroupChannelsMenu - fix requestFocus
$old = "    private void showGroupChannelsMenu() {
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

$new = "    private void showGroupChannelsMenu() {
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
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change D: onGroupItemClicked
$old = "    private void onGroupItemClicked(int position) {
        Log.d(TAG, \"onGroupItemClicked pos=\" + position + \" totalGroups=\" + menuGroups.size());
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

$new = "    private void onGroupItemClicked(int position) {
        Log.d(TAG, \"onGroupItemClicked pos=\" + position + \" totalGroups=\" + menuGroups.size());
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
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change E: dispatchKeyEvent DPAD_RIGHT
$old = "                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        channelRecyclerView.requestFocus();
                        refreshMenuAdapters();
                    } else"
$new = "                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
                            channelListContainer.requestFocus();
                        } else if (channelRecyclerView != null) {
                            channelRecyclerView.requestFocus();
                        }
                        refreshMenuAdapters();
                    } else"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change F: handleTouchSwipe
$old = "                if (dx < 0) {
                    focusOnGroupSide = false;
                    channelRecyclerView.requestFocus();
                    refreshMenuAdapters();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                    refreshMenuAdapters();
                }"
$new = "                if (dx < 0) {
                    focusOnGroupSide = false;
                    if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
                        channelListContainer.requestFocus();
                    } else if (channelRecyclerView != null) {
                        channelRecyclerView.requestFocus();
                    }
                    refreshMenuAdapters();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                    refreshMenuAdapters();
                }"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change G: GestureTraceView onTouchEvent
$old = "                            } else if (channelRecyclerView != null) {
                                channelRecyclerView.scrollBy(0, (int) -stepDy);
                            }"
$new = "                            } else {
                                View sv = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : (View)channelRecyclerView;
                                if (sv != null) sv.scrollBy(0, (int) -stepDy);
                            }"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change H: buildMenu onScrollListener
$old = "        if (Build.VERSION.SDK_INT < 20) {
            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {"
$new = "        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView != null) {
            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change I: buildMenu setOnTouchListener
$old = "        channelRecyclerView.setOnTouchListener(new View.OnTouchListener() {"
$new = "        View channelTouchTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : (View)channelRecyclerView;
        channelTouchTarget.setOnTouchListener(new View.OnTouchListener() {"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change J: detectBadChannels callback
$old = "                        if (cnt > 0) {
                            channelItemAdapter.notifyItemRangeChanged(0, cnt);
                        }
                        groupAdapter.notifyDataSetChanged();"
$new = "                        if (cnt > 0 && Build.VERSION.SDK_INT >= 20) {
                            channelItemAdapter.notifyItemRangeChanged(0, cnt);
                        }
                        groupAdapter.notifyDataSetChanged();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++ }

# Change K: Add ScrollView import
if (-not $content.Contains("import android.widget.ScrollView;")) {
    $content = $content.Replace("import android.widget.ProgressBar;`r`n", "import android.widget.ProgressBar;`r`nimport android.widget.ScrollView;`r`n")
    $count++
}

[System.IO.File]::WriteAllText($path, $content)
Write-Host "Applied $count changes"