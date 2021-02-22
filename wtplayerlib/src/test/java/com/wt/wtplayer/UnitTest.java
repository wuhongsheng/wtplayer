package com.wt.wtplayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author whs
 * @date 2020/9/8
 */
public class UnitTest {
    private List<Integer> temp = new ArrayList<>();
    private List<List<Integer>> ans = new ArrayList<>();


    @Test
    public void testCombine(){
        combine(4,2);
        System.out.println(ans);
    }

    private List<List<Integer>> combine(Integer n, Integer k){
        dfs(1,n, k);
        return ans ;
    }



    /**
     * 深度遍历
     */
    private void dfs(Integer cur,Integer n,Integer k){
        // 剪枝：temp 长度加上区间 [cur, n] 的长度小于 k，不可能构造出长度为 k 的 temp
        if(temp.size() + (n - cur + 1) < k){
            return;
        }
        // 记录合法的答案
        if(temp.size() == k){
            ans.add(new ArrayList<Integer>(temp));
            return;
        }
        //考虑选择当前位置
        temp.add(cur);
        dfs(cur + 1, n, k);
        temp.remove(temp.size() - 1);
        // 考虑不选择当前位置
        dfs(cur + 1, n, k);
    }



}
