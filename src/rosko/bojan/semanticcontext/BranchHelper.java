package rosko.bojan.semanticcontext;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by rols on 5/9/17.
 */
public class BranchHelper {

    class ForStruct {
        int forTrueConditionJump, forFalseConditionJump, forConditionAddress, forIterationAddress;
        ArrayList<Integer> forBreakStatements;
    }

    class IfStruct {
        int adrFalseJump, adrTrueJump;
    }


    private Stack<IfStruct> ifBranches;
    private Stack<ForStruct> forBranches;

    public BranchHelper() {
        ifBranches = new Stack<>();
        forBranches = new Stack<>();
    }

    ForStruct getCurrentFor(){
        if (forBranches.empty())
            return null;
        return forBranches.peek();
    }

    boolean inFor(){
        return !forBranches.empty();
    }

    IfStruct getCurrentIf() {
        if (ifBranches.empty())
            return null;
        return ifBranches.peek();
    }

    void closeCurrentFor() {
        forBranches.pop();
    }

    void closeCurrentIf() {
        ifBranches.pop();
    }

    void openNewFor() {
        forBranches.push(new ForStruct());
        forBranches.peek().forBreakStatements = new ArrayList<>();
    }

    void openNewIf() {
        ifBranches.push(new IfStruct());
    }
}
