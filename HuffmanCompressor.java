import java.util.ArrayList;
import java.util.Comparator;
import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A class to convert a text file into UTF-8 HuffmanCode using HashMaps
 * @author Ishaan Gupta
 */
public class HuffmanCompressor 
{
  /**
   * Reason for using ArrayList
   * I used ArrayList because I wanted to create Huffman Tree using constant space. Plus, the algorithm I had in my mind required faster access of nodes by index which was possible in ArrayList.
   */
  private static ArrayList<HuffmanNode> list = new ArrayList<>();
  //Root of the Huffman Tree
  private static HuffmanNode HuffmanTree;
  //StringBuilder used for Huffman Encodes
  private static StringBuilder sen = new StringBuilder();
  //Hashmap to store the characters and their frequenciesu
  private static HashMap<Character,Integer> h = new HashMap<>();    
  //HashMap to store the characters and huffman encodes
  private static HashMap<Character,String> hTree = new HashMap<>();
  //Stores the space saved when converted from text file to huffmancodes
  private static long spaceSaved;
  
  private static class HuffmanNode
  {
    private Character inChar;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;
    private HuffmanNode parent;
    
    public HuffmanNode(Character inChar, int frequency, HuffmanNode left, HuffmanNode right)
    {
      this.inChar = inChar;
      this.frequency = frequency; 
      this.left = left;
      this.right = right;
      this.parent = null;
    }
  }
  
  /**
   * Method to read a file and write on a different file in huffman encoding
   * @param String
   * @param String
   * @return String
   */
  public static String huffmanCoder(String inputFileName, String outputFileName)
  {
    //Local variable storing the length of Huffman codes of each character
    long length = 0;
    //Local variable storing the count of characters in Input file
    long count = 0; 
    
    //Helper method 
    fileRead(inputFileName);
    print();
    try
    {
      FileInputStream f = new FileInputStream(inputFileName);
      FileWriter write = new FileWriter(outputFileName);
      
      int read = 0;
      //Reading and writing the files
      while((read = f.read()) != -1)
      {
        Character temp = (char)read;
        write.write(hTree.get(temp));  
        length+= hTree.get(temp).length();
        count++;
      }
      f.close();
      write.close();      
    }
    catch(Exception e)
    {
      return "Input file error";
    }
    //space saving calculation
    spaceSaved = 8*count - length;
    //Helper method to write huffmanCode table and space saved
    writeFile();    
    return "Ok";
  }
  
  /**
   * Method to deal with special characters while writing the file
   * Credit: Neo Huang, EECS 233 TA
   */
  public static String escapeSpecialCharacter(String x) 
  {
    StringBuilder sb = new StringBuilder();
    for (char c : x.toCharArray()) {
      if (c >= 32 && c < 127) sb.append(c);
      else sb.append(" [0x" + Integer.toOctalString(c) + "]");
    }
    return sb.toString();
  }
  
  
  /**
   * Method to merge two nodes 
   * @param HuffmanNode
   * @param HuffmanNode
   * @return HuffmanNode
   */
  private static HuffmanNode mergeNodes(HuffmanNode a, HuffmanNode b)
  {
    if(a.frequency < b.frequency)
    {
      HuffmanNode node = new HuffmanNode(null,(a.frequency + b.frequency),a,b);
      a.parent = node;
      b.parent = node;
      return node;
    }
    else
    {
      HuffmanNode node = new HuffmanNode(null,(a.frequency + b.frequency),b,a);
      a.parent = node;
      b.parent = node;
      return node;      
    }
  }
  
  /**
   * Method to sort the elements of the ArrayList
   */
  public static void sorting()
  {
    list.sort(new Comparator<HuffmanNode>(){
      public int compare(HuffmanNode a, HuffmanNode b) {return a.frequency - b.frequency;}});
  }   
  
  /**
   * Method to convert the ArrayList to Huffman Tree
   * @return HuffmanNode
   */
  private static HuffmanNode createTree()
  {
    //List sorted
    sorting();
    
    int size = list.size();
    //Local variable tracking the gap between nodes as they are merged
    int n = 1;   
    
    //loop to check the presence of more than one nodes
    while(n < size && list.get(n) != null)
    { //Loop to traverse through the ArrayList
      for(int i = 0; i + n < size; i++)
      { //Check the presence of more than one nodes
        if(list.get(i) != null && list.get(i+n) != null)          
        { //Merging of two nodes and storing them at initial nodes
          list.set(i,mergeNodes(list.get(i),list.get(i+n)));
          list.set(i+n,null);
        }
      }
      //Changing the gap between two nodes
      n*= 2;  
    }
    //Finally returning the root of created tree
    return list.get(0);
  }
  
  /**
   * Method to read the Input file and store the information in HashMap
   * @param String
   */
  private static void fileRead(String name)
  {
    try
    {
      FileInputStream f = new FileInputStream(name);
      int read = 0;
      //Loop to traverse through file character-by-character
      while((read = f.read()) != -1)
      {
        Character temp = (char)read;
        //A new character comes up
        if(h.get(temp) == null)
          h.put(temp,1);
        //Changing the frequency of already noted character
        else
          h.replace(temp,h.get(temp) + 1);
      }
      f.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    //Local variable used as counter for loop
    int i = 0;  
    //Creating the set of keys from hashMap
    Set<Character> s = h.keySet();
    //Loop to form HuffmanNodes and adding them into ArrayList
    for(Character c : s)
    {
      list.add(i,new HuffmanNode(c,h.get(c),null,null));
      i++;
    }
    //Storing the root of Created Huffman Tree
    HuffmanTree = createTree();
    //Helper method to calculate huffman codes called
    readTree();    
  }
  
  /**
   * Recursive method to look for Character and returns the String of HuffmanCode
   * @param HuffmanNode 
   * @return String
   */
  private static void myReadTree(HuffmanNode trav)
  {
    //Base case when node with character found
    if(trav.left == null && trav.right == null)
    {
      //Putting detail of character and its code in hashmap
      hTree.put(trav.inChar,sen.toString());  
      
      //When only character in File present
      if(sen.length() == 0)
      {
        sen.append("1");
        hTree.put(trav.inChar,sen.toString());      
      }
      //Delete last bit of code due to change of node from left to right
      else
        sen.deleteCharAt(sen.length() - 1);  
      
      //Traversing from child node to parent node after noting the huffmanCode
      if(trav != HuffmanTree)
        myReadTree(trav.parent);          
    }
    
    else
    { 
      //Traversing into left subtree
      if(trav.frequency > 0)
      {
        sen.append("0");
        //Marking that left subtree has been explored
        trav.frequency *= -1;
        if(trav.left != null)
          myReadTree(trav.left);
      }
      
      //Traversing into right subtree
      else if(trav.frequency != 0)
      {
        sen.append("1");
        //Marking that right subtree has been explored
        trav.frequency = 0;
        if(trav.right != null)
          myReadTree(trav.right);
      }
      
      else 
      {
        //Move from child node to parent
        if(sen.length() != 0)
          sen.deleteCharAt(sen.length() - 1);
        if(trav.parent != null)
          myReadTree(trav.parent);          
      }
    }
  }
  
  
  /**
   * Wrapper method for recursive method
   */
  private static void readTree()
  {
    myReadTree(HuffmanTree);    
  } 
  
  /**
   * Method to print characters their frequencies and huffmanCodes
   */
  private static void print()
  {
    for(Character c: hTree.keySet())      
      System.out.println(escapeSpecialCharacter(" Character: " + c + " Frequency: " + h.get(c) + " Code: " + hTree.get(c)));
  }
  
  public static void main(String args[])
  {
    //Try-catch if only one file name input
    try
    {    
      String inputFile = args[0];
      String outputFile = args[1];
      HuffmanCompressor.huffmanCoder(inputFile,outputFile);
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      System.out.println("Input two file names");
    }   
  }
  
  /**
   * Helper method to write Huffman Code table and space saving on 
   * "huffmanCode.txt" & "spaceSaving.txt" respectively
   */
  private static void writeFile()
  {
    try
    {
      FileWriter write = new FileWriter("huffmanCode.txt");
      FileWriter write2 = new FileWriter("spaceSaving.txt");    
      
      for(Character c: hTree.keySet())      
      {
        String temp = escapeSpecialCharacter(" Character: " + c + " Frequency: " + h.get(c) + " Code: " + hTree.get(c)) + "\n";
        write.write(temp);
        write.write(" ");
      }
      write2.write("Space saved = " + spaceSaved);
      
      write2.close();
      write.close();      
    }
    catch(Exception e)
    {
      System.out.println("Input file error");
    }
  }
  
  
  
  
  
}


