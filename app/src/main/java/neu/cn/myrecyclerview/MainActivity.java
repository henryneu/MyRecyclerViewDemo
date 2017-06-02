package neu.cn.myrecyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 水果数据列表，存放水果数据
    private List<Fruit> fruitList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FruitAdapter fruitAdapter;
    // 处理RecyclerView中Item的滑动和拖拽
    private ItemTouchHelper itemTouchHelper;
    private WindowManager windowManager;

    private Button addItem;
    private Button deleteItem;
    private int screenwidth;
    private Boolean remove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFruits();
        fruitAdapter = new FruitAdapter(fruitList);
        remove = false;
        addItem = (Button) findViewById(R.id.add_item);
        deleteItem = (Button) findViewById(R.id.delete_item);
        windowManager = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        screenwidth = outMetrics.widthPixels; // 获取屏幕宽度
        addItem.setOnClickListener(this);
        deleteItem.setOnClickListener(this);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            // 用于设置拖拽和滑动的方向
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0, swipeFlags = 0;
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager || recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    // 瀑布流和网格布局有四个拖拽方向
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                    // 线性布局有两个拖拽方向
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    // 设置侧滑方向为从两个方向都可以
                    swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                }
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            // 长摁Item拖拽时会回调这个方法
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(fruitList, from, to); // 交换fruitList中数据的位置
                fruitAdapter.notifyItemMoved(from, to); // 更新适配器中item的位置
                return true;
            }

            // 处理滑动删除操作
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                fruitAdapter.deleteItem(viewHolder.getAdapterPosition());
                fruitAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true; // 返回true则为所有Item都设置可以拖拽
            }

            // 当Item拖拽开始时调用
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        viewHolder.itemView.setElevation(100);
                    }
                }
            }

            // 当Item拖拽完成时调用
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.itemView.setElevation(0);
                }
            }

            // 当Item视图变化时调用
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                viewHolder.itemView.scrollTo(-(int) dX, -(int) dY); // 根据Item的滑动偏移修改HorizontalScrollView的滚动
                if (Math.abs(dX) > screenwidth / 5 && !remove && isCurrentlyActive) {
                    // 用户收滑动Item超过屏幕5分之1，标记为要删除
                    remove = true;
                } else if (Math.abs(dX) < screenwidth / 5 && remove && !isCurrentlyActive) {
                    // 用户收滑动Item没有超过屏幕5分之1，标记为不删除
                    remove = false;
                }
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && remove == true && !isCurrentlyActive) {
                    // 当用户滑动Item超过屏幕5分之1，并且松手时，执行删除Item
                    if (viewHolder != null && viewHolder.getAdapterPosition() >= 0) {
                        fruitAdapter.deleteItem(viewHolder.getAdapterPosition());
                        remove = false;
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // 与RecyclView绑定
        itemTouchHelper.attachToRecyclerView(recyclerView);
        // 线性布局
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); // 改变RecyclerView的方向
        // 网格布局
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 第二个参数表示每行显示的个数
        // 瀑布流布局
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fruitAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // 设置Item加载或移除时的动画
    }

    /**
     * 初始化水果数据列表
     */
    private void initFruits() {
        for (int i = 0; i < 2; i++) {
            Fruit apple = new Fruit(R.drawable.apple_pic, getRandomName("Apple"));
            fruitList.add(apple);
            Fruit banana = new Fruit(R.drawable.banana_pic, getRandomName("Banana"));
            fruitList.add(banana);
            Fruit orange = new Fruit(R.drawable.orange_pic, getRandomName("Orange"));
            fruitList.add(orange);
            Fruit watermelon = new Fruit(R.drawable.watermelon_pic, getRandomName("Watermelon"));
            fruitList.add(watermelon);
            Fruit pear = new Fruit(R.drawable.pear_pic, getRandomName("Pear"));
            fruitList.add(pear);
            Fruit grape = new Fruit(R.drawable.grape_pic, getRandomName("Grape"));
            fruitList.add(grape);
            Fruit pineapple = new Fruit(R.drawable.pineapple_pic, getRandomName("Pineapple"));
            fruitList.add(pineapple);
            Fruit strawberry = new Fruit(R.drawable.strawberry_pic, getRandomName("Strawberry"));
            fruitList.add(strawberry);
            Fruit cherry = new Fruit(R.drawable.cherry_pic, getRandomName("Cherry"));
            fruitList.add(cherry);
            Fruit mango = new Fruit(R.drawable.mango_pic, getRandomName("Mango"));
            fruitList.add(mango);
        }
    }

    private String getRandomName(String name) {
        StringBuffer buffer = new StringBuffer();
        Random random = new Random(20);
        int length = random.nextInt(20);
        for (int i = 0; i < length; i++) {
            buffer.append(name);
        }
        return buffer.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_item: // RecyclerView中添加Item
                fruitAdapter.addItem(fruitList.size());
                break;
            case R.id.delete_item: // // RecyclerView中删除Item
                fruitAdapter.deleteItem(fruitList.size() - 1);
                break;
            default:
                break;
        }
    }
}
