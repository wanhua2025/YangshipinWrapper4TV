$path = "f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"
$content = [System.IO.File]::ReadAllText($path)
$count = 0

# Change 1: safeNotifyChannelAdapter
$old = "if (channelItemAdapter == null || channelRecyclerView == null) return;`r`n        if (Build.VERSION.SDK_INT < 20) {"
$new = "if (channelItemAdapter == null) return;`r`n        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView == null) return;`r`n        if (Build.VERSION.SDK_INT < 20) {"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "1:OK" } else { Write-Host "1:FAIL" }

# Change 2: forceRebindAllVisibleItems - add API 19 early return
$old = "private void forceRebindAllVisibleItems() {`r`n        if (channelRecyclerView == null || channelItemAdapter == null) return;"
$new = "private void forceRebindAllVisibleItems() {`r`n        if (Build.VERSION.SDK_INT < 20) return;`r`n        if (channelRecyclerView == null || channelItemAdapter == null) return;"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "2:OK" } else { Write-Host "2:FAIL" }

# Change 3: manualRenderChannelList - use channelListInnerLayout
$old = "channelRecyclerView == null || channelItemAdapter == null) return;`r`n        // 仅在菜单可见时才重建视图"
$new = "channelItemAdapter == null) return;`r`n        if (channelListInnerLayout == null) return;`r`n        // 仅在菜单可见时才重建视图"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "3:OK" } else { Write-Host "3:FAIL" }

# Change 4: manualRenderChannelList - replace channelRecyclerView references with channelListInnerLayout
$old = "View firstChild = channelRecyclerView.getChildCount() > 0 ? channelRecyclerView.getChildAt(0) : null;"
$new = "View firstChild = channelListInnerLayout.getChildCount() > 0 ? channelListInnerLayout.getChildAt(0) : null;"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "4:OK" } else { Write-Host "4:FAIL" }

$old = "firstVisiblePos = channelRecyclerView.getChildAdapterPosition(firstChild);"
$new = "firstVisiblePos = 0;"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "5:OK" } else { Write-Host "5:FAIL" }

$old = "channelRecyclerView.removeAllViews();"
$new = "channelListInnerLayout.removeAllViews();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "6:OK" } else { Write-Host "6:FAIL" }

$old = "channelRecyclerView.addView(container);`r`n        }`r`n        // 尝试保持滚动位置"
$new = "channelListInnerLayout.addView(container);`r`n        }`r`n        // 尝试保持滚动位置"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "7:OK" } else { Write-Host "7:FAIL" }

$old = "int childCount = channelRecyclerView.getChildCount();`r`n                if (childCount > scrollTarget)"
$new = "int childCount = channelListInnerLayout.getChildCount();`r`n                if (childCount > scrollTarget)"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "8:OK" } else { Write-Host "8:FAIL" }

$old = "View target = channelRecyclerView.getChildAt(Math.min(scrollTarget, childCount - 1));"
$new = "View target = channelListInnerLayout.getChildAt(Math.min(scrollTarget, childCount - 1));"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "9:OK" } else { Write-Host "9:FAIL" }

$old = "channelRecyclerView.scrollTo(0, Math.max(0, top - dp(4)));"
$new = "channelListContainer.scrollTo(0, Math.max(0, top - dp(4)));"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "10:OK" } else { Write-Host "10:FAIL" }

$old = "channelRecyclerView.requestLayout();`r`n                channelRecyclerView.invalidate();"
$new = "channelListContainer.requestLayout();`r`n                channelListContainer.invalidate();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "11:OK" } else { Write-Host "11:FAIL" }

# Change 12: scrollChannelToPosition for API 19
$old = "if (channelRecyclerView == null) return;`r`n        if (position < 0) position = 0;`r`n        int total = channelItemAdapter.getItemCount();`r`n        if (position >= total) position = Math.max(0, total - 1);`r`n        if (Build.VERSION.SDK_INT < 20) {`r`n            // API 19: 手动渲染模式下，用 requestFocus + scrollTo`r`n            final int targetPos = position;`r`n            channelRecyclerView.post(new Runnable() {`r`n                @Override`r`n                public void run() {`r`n                    int childCount = channelRecyclerView.getChildCount();`r`n                    if (childCount > 0 && targetPos < childCount) {`r`n                        View target = channelRecyclerView.getChildAt(targetPos);"
$new = "channelRecyclerView) return;`r`n        if (position < 0) position = 0;`r`n        if (Build.VERSION.SDK_INT < 20) {`r`n            if (channelListContainer == null || channelListInnerLayout == null) return;`r`n            int total = channelItemAdapter.getItems().size();`r`n            if (position >= total) position = Math.max(0, total - 1);`r`n            // API 19: 用 scrollTo 滚动 ScrollView`r`n            final int targetPos = position;`r`n            channelListContainer.post(new Runnable() {`r`n                @Override`r`n                public void run() {`r`n                    int childCount = channelListInnerLayout.getChildCount();`r`n                    if (childCount > 0 && targetPos < childCount) {`r`n                        View target = channelListInnerLayout.getChildAt(targetPos);"
# Let me skip this complex change and do it differently
Write-Host "12:SKIP"

# Change 13: showGroupChannelsMenu - channelRecyclerView.requestFocus()
$old = "channelRecyclerView.requestFocus();`r`n        resetMenuAutoHide();`r`n    }`r`n`r`n    private void showSettingsMenu()"
$new = "if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {`r`n            channelListContainer.requestFocus();`r`n        } else if (channelRecyclerView != null) {`r`n            channelRecyclerView.requestFocus();`r`n        }`r`n        resetMenuAutoHide();`r`n    }`r`n`r`n    private void showSettingsMenu()"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "13:OK" } else { Write-Host "13:FAIL" }

# Change 14: onGroupItemClicked - channelRecyclerView.requestFocus()
$old = "focusOnGroupSide = false;`r`n            channelRecyclerView.requestFocus();"
$new = "focusOnGroupSide = false;`r`n            if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) { channelListContainer.requestFocus(); } else if (channelRecyclerView != null) { channelRecyclerView.requestFocus(); }"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "14:OK" } else { Write-Host "14:FAIL" }

# Change 15: dispatchKeyEvent DPAD_RIGHT - channelRecyclerView.requestFocus()
$old = "focusOnGroupSide = false;`r`n                        channelRecyclerView.requestFocus();`r`n                        refreshMenuAdapters();"
$new = "focusOnGroupSide = false;`r`n                        if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) { channelListContainer.requestFocus(); } else if (channelRecyclerView != null) { channelRecyclerView.requestFocus(); }`r`n                        refreshMenuAdapters();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "15:OK" } else { Write-Host "15:FAIL" }

# Change 16: handleTouchSwipe - channelRecyclerView.requestFocus()
$old = "focusOnGroupSide = false;`r`n                    channelRecyclerView.requestFocus();`r`n                    refreshMenuAdapters();"
$new = "focusOnGroupSide = false;`r`n                    if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) { channelListContainer.requestFocus(); } else if (channelRecyclerView != null) { channelRecyclerView.requestFocus(); }`r`n                    refreshMenuAdapters();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "16:OK" } else { Write-Host "16:FAIL" }

# Change 17: GestureTraceView onTouchEvent - channelRecyclerView.scrollBy()
$old = "} else if (channelRecyclerView != null) {`r`n                                channelRecyclerView.scrollBy(0, (int) -stepDy);"
$new = "} else {`r`n                                View sv = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;`r`n                                if (sv != null) sv.scrollBy(0, (int) -stepDy);"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "17:OK" } else { Write-Host "17:FAIL" }

# Change 18: buildMenu - remove onScrollListener for API 19
$old = "if (Build.VERSION.SDK_INT < 20) {`r`n            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {"
$new = "if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView != null) {`r`n            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "18:OK" } else { Write-Host "18:FAIL" }

# Change 19: buildMenu - channelRecyclerView.setOnTouchListener
$old = "channelRecyclerView.setOnTouchListener(new View.OnTouchListener() {"
$new = "View channelTouchTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : (View)channelRecyclerView;`r`n        channelTouchTarget.setOnTouchListener(new View.OnTouchListener() {"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "19:OK" } else { Write-Host "19:FAIL" }

# Change 20: detectBadChannels - skip notifyItemRangeChanged for API 19
$old = "channelItemAdapter.notifyItemRangeChanged(0, cnt);`r`n                        }`r`n                        groupAdapter.notifyDataSetChanged();"
$new = "if (cnt > 0 && Build.VERSION.SDK_INT >= 20) { channelItemAdapter.notifyItemRangeChanged(0, cnt); }`r`n                        }`r`n                        groupAdapter.notifyDataSetChanged();"
if ($content.Contains($old)) { $content = $content.Replace($old, $new); $count++; Write-Host "20:OK" } else { Write-Host "20:FAIL" }

# Change 21: Add ScrollView import if missing
if (-not $content.Contains("import android.widget.ScrollView;")) {
    $content = $content.Replace("import android.widget.ProgressBar;", "import android.widget.ProgressBar;`r`nimport android.widget.ScrollView;")
    $count++; Write-Host "21:OK"
} else { Write-Host "21:SKIP" }

[System.IO.File]::WriteAllText($path, $content)
Write-Host "`nTotal changes: $count"