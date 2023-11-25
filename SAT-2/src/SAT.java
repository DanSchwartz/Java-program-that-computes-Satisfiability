import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;

public class SAT {
    public static SortedSet<String> expressionVariables = new TreeSet();
    public static ArrayList<TreeNode> expressionVariableNodes = new ArrayList();

    public static void main(String[] args) throws Exception {
        String theExpression = readFileAsString("..\\expression.txt");
        System.out.println("theExpression = " + theExpression);
        TreeNode parseTree = parse(theExpression);
//        System.out.println("print parseTree");
//        printParseTree(parseTree);
//        printExpressionVariables(expressionVariables);
        expressionVariableNodes = cleanAndSortExpressionVariableNodes(expressionVariables, expressionVariableNodes);
//        printExpressionVariableNodes(expressionVariableNodes);
        Boolean[][] truthValueTuples = createTruthValueTuples(expressionVariableNodes.size());
//        printTruthValueTuples(truthValueTuples);
        String theAnswer = computeSat(parseTree, expressionVariableNodes, truthValueTuples);
        System.out.println("theAnswer = " + theAnswer);
    }

    public static String readFileAsString(String fileName)
            throws Exception {
        String data = "";
        data = new String(
                Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public static TreeNode parse(String theExpression) {
        if (theExpression.charAt(0) == 101) {  //letter e, the expression is a single expression variable
            TreeNode expressionVariableNode = new TreeNode(theExpression);
            expressionVariableNodes.add(expressionVariableNode);
            expressionVariables.add(expressionVariableNode.getContent());
            return expressionVariableNode;
        }
        if (theExpression.charAt(0) == 40) {  //the first char is (
            theExpression = theExpression.substring(1, theExpression.length() - 1); //remove surrounding parens
        }
        if (theExpression.charAt(0) == 126) {  //found ~
            theExpression = theExpression.substring(1, theExpression.length());  //remove ~
            TreeNode negationNode = new TreeNode(String.valueOf((char) (126)));
            negationNode.addChild(parse(theExpression));
            return negationNode;
        }
        if (theExpression.charAt(0) == 101) {  //letter e, the expression is a variable followed by a connective
            int endOfVariable = theExpression.indexOf(' ');
            String theVariable = theExpression.substring(0, endOfVariable);
            String firstSubexpression = theVariable;
            char connective = theExpression.charAt(endOfVariable + 1);
            TreeNode connectiveNode = new TreeNode(String.valueOf(connective));
            String secondSubexpression = theExpression.substring(endOfVariable + 3, theExpression.length());
            connectiveNode.addChild(parse(firstSubexpression));
            connectiveNode.addChild(parse(secondSubexpression));
            return connectiveNode;
        }
        if (theExpression.charAt(0) == 40) {
            //found left paren, the expression has a complex subexpression on the left of some connective
            int leftParenCount = 1;
            int rightParenCount = 0;
            int charIndex = 1;
            while (leftParenCount != rightParenCount) {
                if (theExpression.charAt(charIndex) == 40) {
                    leftParenCount++;
                }
                if (theExpression.charAt(charIndex) == 41) {
                    rightParenCount++;
                }
                charIndex++;
            } //charIndex is position of matching right paren
            String firstSubexpression = theExpression.substring(0, charIndex);
            char connective = theExpression.charAt(charIndex + 1);
            TreeNode connectiveNode = new TreeNode(String.valueOf(connective));
            String secondSubexpression = theExpression.substring(charIndex + 3, theExpression.length());
            connectiveNode.addChild(parse(firstSubexpression));
            connectiveNode.addChild(parse(secondSubexpression));
            return connectiveNode;
        }
        return null;
    }

    public static void printParseTree(TreeNode parseTree) {
        //depth-first left-right search
        ArrayList<TreeNode> open = new ArrayList();
        open.add(parseTree);
        ArrayList<TreeNode> closed = new ArrayList();
        while (!open.isEmpty()) {
            TreeNode x = open.get(0);
            open.remove(0);
            System.out.println("x = " + x.getContent());
            ArrayList<TreeNode> children = new ArrayList(x.getChildNodes());
            if (!children.isEmpty()) {
                System.out.println("children start");
                for (int i = 0; i < children.size(); i++) {
                    System.out.println(children.get(i).getContent());
                    System.out.println("parent = " + children.get(i).getParent().getContent());
                }
                System.out.println("children end");
            }
            closed.add(x);
            for (int i = 0; i < children.size(); i++) {
                TreeNode theChild = children.get(i);
                if (!inList(open, theChild) && !inList(closed, theChild) && theChild.getContent().charAt(0) != 101) {
                    open.add(theChild);
                }
            }
        }
    }

    public static Boolean inList(ArrayList<TreeNode> theList, TreeNode theElement) {
        for (int i = 0; i < theList.size(); i++) {
            if (theElement == theList.get(i)) {
                return true;
            }
        }
        return false;
    }

    public static void printExpressionVariables(SortedSet<String> expressionVariables) {
        System.out.println("Expression Variables");
        for (String variable : expressionVariables) {
            System.out.println(variable);
        }
    }

    public static ArrayList cleanAndSortExpressionVariableNodes(SortedSet<String> expressionVariables,
                                                                ArrayList<TreeNode> expressionVariableNodes) {
        //removes duplicate nodes and puts them in the same order as the sorted set (numerical order)
        ArrayList<TreeNode> newExpressionVariableNodesList = new ArrayList();
        for (String variable : expressionVariables) {
            for (int i = 0; i < expressionVariableNodes.size(); i++) {
                if (expressionVariableNodes.get(i).getContent() == variable) {
                    newExpressionVariableNodesList.add(expressionVariableNodes.get(i));
                }
            }
        }
        return newExpressionVariableNodesList;
    }

    public static void printExpressionVariableNodes(ArrayList<TreeNode> expressionVariableNodes) {
        System.out.println("Expression Variable Nodes");
        for (TreeNode variableNode : expressionVariableNodes) {
            System.out.println(variableNode.getContent());
        }
    }

    public static Boolean[][] createTruthValueTuples(int numberOfExpressionVariables) {
        int numberOfColumns = numberOfExpressionVariables;
        int numberOfRows = (int) Math.pow(2, numberOfExpressionVariables);
        Boolean[][] truthValueTuples = new Boolean[numberOfRows][numberOfColumns];
        // The i-th column, i=1,2,..., is divided into 2^i groups with each group having 2^(numberOfColumns-i)
        // truth values, with the groups alternating all "true" and all "false".
        for (int i = 1; i <= numberOfColumns; i++) {
            int numberOfGroups = (int) Math.pow(2, i);
            ArrayList<ArrayList> groups = new ArrayList();
            for (int j = 1; j <= numberOfGroups; j++) {
                ArrayList<Boolean> newGroup = new ArrayList();
                if (j % 2 == 1) {
                    for (int k = 1; k <= Math.pow(2, numberOfColumns - i); k++) {
                        newGroup.add(true);
                    }
                } else {
                    for (int k = 1; k <= Math.pow(2, numberOfColumns - i); k++) {
                        newGroup.add(false);
                    }
                }
                groups.add(newGroup);
            }
            ArrayList<Boolean> allGroups = new ArrayList();
            for (int j = 0; j < numberOfGroups; j++) {
                allGroups.addAll(groups.get(j));
            }
            for (int j = 0; j < numberOfRows; j++) {
                truthValueTuples[j][i - 1] = allGroups.get(j);
            }
        }
        return truthValueTuples;
    }

    public static void printTruthValueTuples(Boolean[][] truthValueTuples) {
        int numberOfColumns = truthValueTuples[0].length;
        int numberOfRows = (int) Math.pow(2, numberOfColumns);
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                System.out.print(truthValueTuples[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static Boolean evaluateTuple(TreeNode treeNode, ArrayList<TreeNode> expressionVariableNodes,
                                        ArrayList<Boolean> tuple) {
        for (int i = 0; i < expressionVariableNodes.size(); i++) {
            expressionVariableNodes.get(i).setTruthValue(tuple.get(i));
        }
        if (treeNode.getContent().startsWith("e")) {
            String variable = treeNode.getContent();
            int variableLength = variable.length();
            String variableNumber = variable.substring(1, variableLength);
            int variableIndex = Integer.parseInt(variableNumber);
            Boolean variableValue = expressionVariableNodes.get(variableIndex - 1).getTruthValue();
            return variableValue;
        } else {
            if (treeNode.getContent().equals("~")) {
                List<TreeNode> children = treeNode.getChildNodes();
                TreeNode theChild = children.get(0);
                Boolean childValue = evaluateTuple(theChild, expressionVariableNodes, tuple);
                return !childValue;
            } else {
                if (treeNode.getContent().equals("V")) {
                    List<TreeNode> children = treeNode.getChildNodes();
                    TreeNode firstChild = children.get(0);
                    TreeNode secondChild = children.get(1);
                    Boolean firstChildValue = evaluateTuple(firstChild, expressionVariableNodes, tuple);
                    Boolean secondChildValue = evaluateTuple(secondChild, expressionVariableNodes, tuple);
                    return firstChildValue || secondChildValue;
                } else {
                    if (treeNode.getContent().equals("&")) {
                        List<TreeNode> children = treeNode.getChildNodes();
                        TreeNode firstChild = children.get(0);
                        TreeNode secondChild = children.get(1);
                        Boolean firstChildValue = evaluateTuple(firstChild, expressionVariableNodes, tuple);
                        Boolean secondChildValue = evaluateTuple(secondChild, expressionVariableNodes, tuple);
                        return firstChildValue & secondChildValue;
                    }
                }
            }
        }
        return null;
    }

    public static String computeSat(TreeNode treeNode, ArrayList<TreeNode> expressionVariableNodes,
                                    Boolean[][] truthValueTuples) {
        int numberOfColumns = truthValueTuples[0].length;
        int numberOfRows = (int) Math.pow(2, numberOfColumns);
        for (int i = 0; i < numberOfRows; i++) {
            ArrayList<Boolean> tuple = new ArrayList();
            for (int j = 0; j < numberOfColumns; j++) {
                tuple.add(truthValueTuples[i][j]);
            }
//            System.out.println("tuple = " + tuple);
            Boolean rowResult = evaluateTuple(treeNode, expressionVariableNodes, tuple);
//            System.out.println("rowResult = " + rowResult);
            if (rowResult == true) {
                return "Satisfiable";
            }
        }
        return "Not Satisfiable";
    }

}

