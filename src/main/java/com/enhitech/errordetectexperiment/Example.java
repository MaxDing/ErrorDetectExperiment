package com.enhitech.errordetectexperiment;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用z3的求解器获取矛盾核的简单示例
 */
public class Example {
    public static void main(String[] args) {
        /*
            f = (a->b) and (b->c) and  (b) and (not(c)) and (d->e)
            这里面(b->c)  (b) and (not(c))就是矛盾核
         */
        Context context = new Context();
        Solver solver = context.mkSolver();
        //首先定义所有的bool变量
        BoolExpr a = context.mkBoolConst("a");
        BoolExpr b = context.mkBoolConst("b");
        BoolExpr c = context.mkBoolConst("c");
        BoolExpr d = context.mkBoolConst("d");
        BoolExpr e = context.mkBoolConst("e");
        //然后根据bool变量生成所有的表达式
        BoolExpr expr1 = context.mkImplies(a,b);
        BoolExpr expr2 = context.mkImplies(b,c);
        BoolExpr expr3 = b;
        BoolExpr expr4 = context.mkNot(c);
        BoolExpr expr5 = context.mkImplies(d,e);
        //z3的unsatCore 要求原始表达式和一个bool变量来track该表达式
        Map<BoolExpr,BoolExpr> label2ExprMap = new HashMap<>();
        label2ExprMap.put(context.mkBoolConst("expr1"),expr1);
        label2ExprMap.put(context.mkBoolConst("expr2"),expr2);
        label2ExprMap.put(context.mkBoolConst("expr3"),expr3);
        label2ExprMap.put(context.mkBoolConst("expr4"),expr4);
        label2ExprMap.put(context.mkBoolConst("expr5"),expr5);
        /*接着将这些表达式加入求解器*/
        for(Map.Entry<BoolExpr,BoolExpr> entry: label2ExprMap.entrySet()){
            BoolExpr labelExpr = entry.getKey();
            BoolExpr sourceExpr = entry.getValue();
            /*原始表达式，track的bool变量*/
            solver.assertAndTrack(sourceExpr,labelExpr);
        }
        /*solver 进行可满足性的运算*/
        Status status =  solver.check();
        if(status == Status.SATISFIABLE){
            System.out.println("Expressions are satisfiable!");
        }
        else if(status == Status.UNSATISFIABLE) {
            BoolExpr[] unsatCore = solver.getUnsatCore();
            System.out.println("Expressions are unsatisfiable and the unsat core is:");
            for(BoolExpr labelExpr:unsatCore){
                BoolExpr sourceExpr = label2ExprMap.get(labelExpr);
                System.out.println(sourceExpr.toString());
            }
        }
        else{
            System.out.println("The result is unknown!");
        }


    }
}
