package com.wt.wtplayer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCombine(){
        combine(4,2)
        println(ans)
    }



    /**
     * 给定两个整数 n 和 k，返回 1 ... n 中所有可能的 k 个数的组合。
     *
     */
    fun combine(n: Int, k: Int): List<List<Int>> {
        dfs(1,n, k)
        return ans
    }

    /**
     * 深度遍历
     */
    fun dfs(cur: Int,n: Int,k: Int){
        // 剪枝：temp 长度加上区间 [cur, n] 的长度小于 k，不可能构造出长度为 k 的 temp
        if(temp.size + (n - cur + 1) < k){
            return
        }
        // 记录合法的答案
        if(temp.size == k){
            println(temp)
            ans.add(temp.toList())
            return
        }
        //考虑选择当前位置
        temp.add(cur)
        dfs(cur + 1, n, k)
        temp.removeAt(temp.size - 1)
        // 考虑不选择当前位置
        dfs(cur + 1, n, k)
    }


    private var tempArray:IntArray = intArrayOf()



    private var temp:MutableList<Int> = mutableListOf()
    private var ans:MutableList<List<Int>> = mutableListOf()




    /**
     * 39. 组合总和
     */
    fun combinationSum(candidates: IntArray, target: Int): List<List<Int>> {
        combinationSumDfs(candidates,0,target)
        return ans
    }

    @Test
    fun testCombinationSum(){
        var candidates = intArrayOf(2,3,6,7)
        combinationSum(candidates,7)
        println(ans)
    }

    fun combinationSumDfs(candidates: IntArray,cur: Int,target: Int){
        if(cur == candidates.size){
            return
        }
        if(target == 0){
            println(temp)
            ans.add(temp.toList())
            return
        }
        //调过当前数
        combinationSumDfs(candidates,cur + 1,target)
        //选择当前数
        if (target - candidates[cur] >= 0) {
            temp.add(candidates[cur])
            combinationSumDfs(candidates, cur,target - candidates[cur]);
            temp.removeAt(temp.size - 1)
        }
    }

    /**
     * 40. 组合总和 II
     * candidates 中的每个数字在每个组合中只能使用一次。
     * 解集不能包含重复的组合。
     */
    private var hashSet:LinkedHashSet<Int> = linkedSetOf()

    @Test
    fun testCombinationSum2(){
        var candidates = intArrayOf(10,1,2,7,6,1,5)
        candidates.sort()
        combinationSum2(candidates,8)
        println(ans)
    }

    fun combinationSum2(candidates: IntArray, target: Int): List<List<Int>> {
        combinationSumDfs2(candidates,0,target)
        return ans
    }

    fun combinationSumDfs2(candidates: IntArray,cur: Int,target: Int){
        if(cur == candidates.size || target < 0){
            return
        }
        if(target == 0){
            println(temp)
            ans.add(temp.toList())
            return
        }
        //调过当前数
        combinationSumDfs(candidates,cur + 1,target)
        //选择当前数
        if (target - candidates[cur] >= 0 && !hashSet.contains(cur)) {
            hashSet.add(cur)
            temp.add(candidates[cur])
            combinationSumDfs(candidates, cur,target - candidates[cur]);
            temp.removeAt(temp.size - 1)
        }
    }

}