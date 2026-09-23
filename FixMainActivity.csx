// C# Script to fix MainActivity.java
var path = @"f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java";
var content = System.IO.File.ReadAllText(path);
var orig = content;

// 1. Fix safeNotifyChannelAdapter - remove channelRecyclerView null check
content = ReplaceOne(content,
    "if (channelItemAdapter == null || channelRecyclerView == null) return;\n        if (Build.VERSION.SDK_INT < 20) {",
    "if (channelItemAdapter == null) return;\n        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView == null) return;\n        if (Build.VERSION.SDK_INT < 20) {");

// 2. Fix forceRebindAllVisibleItems - skip for API 19
content = ReplaceOne(content,
    "@android.annotation.TargetApi(19)\n    private void forceRebindAllVisibleItems() {\n        if (channelRecyclerView == null || channelItemAdapter == null) return;",
    "@android.annotation.TargetApi(19)\n    private void forceRebindAllVisibleItems() {\n        if (Build.VERSION.SDK_INT < 20) return;\n        if (channelRecyclerView == null || channelItemAdapter == null) return;");

// 3. Fix manualRenderChannelList - use channelListInnerLayout
var oldManual = @"    private void manualRenderChannelList() {
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
    }";

var newManual = @"    private void manualRenderChannelList() {
        if (channelItemAdapter == null) return;
        if (channelListInnerLayout == null) return;
        // 仅在菜单可见时才重建视图（避免后台重建大量View浪费内存）
        if (menuPanel.getVisibility() != View.VISIBLE) return;
        // 记录列表可见区域顶部位置
        int firstVisiblePos = 0;
        View firstChild = channelListInnerLayout.getChildCount() > 0 ? channelListInnerLayout.getChildAt(0) : null;
        if (firstChild != null) {
            firstVisiblePos = (int) firstChild.GetTag(0x1001);
            if (firstVisiblePos < 0) firstVisiblePos = 0;
        }
        // 移除所有子视图
        channelListInnerLayout.removeAllViews();
        List<Channel> items = channelItemAdapter.getItems();
        if (items == null || items.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            Channel ch = items.get(i);
            LinearLayout container = channelItemAdapter.createItemView(ch, i);
            container.SetTag(0x1001, i);
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
    }";

content = content.Replace(oldManual, newManual);

Console.WriteLine("Changes applied: " + (content != orig ? "YES" : "NO - strings not matched"));

static string ReplaceOne(string text, string oldStr, string newStr) {
    int idx = text.IndexOf(oldStr, StringComparison.Ordinal);
    if (idx >= 0) {
        Console.WriteLine("Match found at index " + idx);
        return text.Substring(0, idx) + newStr + text.Substring(idx + oldStr.Length);
    } else {
        Console.WriteLine("NOT FOUND: " + oldStr.Substring(0, Math.Min(40, oldStr.Length)));
        return text;
    }
}