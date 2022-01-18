package ics211_EC_Webcrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a WebCrawler.
 * @author Louie Bala
 *        Partners - Gavin Peng, Londy Tong Lee, Callandra Ruiter, William Liang.
 *        ATA - Kana'i Gooding
 */
public class WebCrawler {
  
  public static HashMap<String, Node> graph = new HashMap();
  
  /**
   * the main method.
   * @param args the args.
   */
  public static void main(String[] args) {

    try {

      Node node = new Node(args[0], 0);
      
      bfs(node, Integer.parseInt(args[1]));
      reset();
      dfs(node, Integer.parseInt(args[1]));
      
      System.out.println("Processed finished");

    } catch (IOException e) {
      System.out.println("Error : invalid web address \n"
          + "Usage : \nWebCrawler [ valid web address ] [ depth to be searched ]\n"); 
    }
    
  }

  private static void bfs(Node n, int maxDepth) {
    Deque<Node> theQueue = new LinkedList<>();
    theQueue.offer(n);
    Node currentNode;
    System.out.println("BFS RUN =================================== ");
    while (!theQueue.isEmpty()) {

      currentNode = theQueue.poll();

      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {
        graph.put(currentNode.web, currentNode);
        currentNode.isVisited = true;
        System.out.println("Visited: " + currentNode.web + " Depth: " + currentNode.depth);
        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            theQueue.offer(temp);
          }
        }
      }
    }
  }


  private static void dfs(Node n, int maxDepth) {
    Stack<Node> theStack = new Stack<>();
    Stack<String> revP = new Stack<>();
    theStack.push(n);
    Node currentNode;
    System.out.println("DFS RUN =================================== ");
    while (!theStack.empty()) {

      currentNode = theStack.pop();

      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {

        currentNode.isVisited = true;
        graph.put(currentNode.web, currentNode);
        revP.push("Visited: " + currentNode.web + " Depth: " + currentNode.depth);
        
        for (Node temp : currentNode.getChildren()) {
          if (temp.depth <= maxDepth) {
            theStack.push(temp);
          }
        }
      }
    }
    while (!revP.empty()) {
      System.out.println(revP.pop());
    }
  }
  
  private static void reset() {
    for (Node node : graph.values()) {
      node.isVisited = false;
    }
    System.out.println("\n\n\n");
  }


  private static class Node {
    ArrayList<Node> childLinks; 
    private String web; //string of associated website
    public boolean isVisited; //true if website has been visited
    private int depth;

    Node(String url,int depth) throws IOException {
      if (!validateLink(url) && !tryConnection(url)) {
        throw new IOException();
      }
      this.web = url;
      this.depth = depth;
    }

    private static boolean validateLink(String url) {
      try {
        // Website input argument 
        String website = url;
        // Regex to check valid URL
        // Found this at https://www.geeksforgeeks.org/check-if-an-url-is-valid-or-not-using-regular-expression/
        String regex = "((http|https)://)(www.)[a-z"
            + "A-Z0-9@:%._\\+~#?&//=]{2,256}\\.(com|edu|mil|gov|org)"
            + "\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
        // Compile pattern
        Pattern pattern = Pattern.compile(regex);
        // Finds match between regex string and user input website string
        Matcher websiteMatch = pattern.matcher(website);
        // If website is invalid, throw error to user
        if (!websiteMatch.matches()) {
          return false;
        }
        return true;
      } catch (NumberFormatException e) { // Catches invalid depth crawl input i.e letters
        return false;
      }
    }

    private static boolean tryConnection(String url) {

      try {
        new URL(url).toURI();
      } catch (MalformedURLException | URISyntaxException e) {
        return false;
      }

      return true;
    }
    
    private String getWebsite(String url) throws IOException, InterruptedException {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    }

    private ArrayList<String> getLinks(String website) {
      // https://stackoverflow.com/questions/5120171/extract-links-from-a-web-page

      ArrayList<String> result = new ArrayList<String>();

      String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(website);
      while (m.find())
      {
        result.add(m.group());
      }

      return result;
    }

    private ArrayList<Node> getChildren() {
      if (this.childLinks != null) {
        return childLinks;
      }
      ArrayList<Node> listOfChildren = new ArrayList<Node>();

      try {
        ArrayList<String> listOfLinks = getLinks(getWebsite(web));
        for (String link : listOfLinks) {
          try {
            Node child = new Node(link, depth + 1);
            listOfChildren.add(child);
          } catch (IOException e) {
            // don't do anything
          }
        }
      } catch (IOException e) {
        // dont do anything
      } catch (InterruptedException e) {
        // dont do anything
      }
      this.childLinks = listOfChildren;
      return listOfChildren;
    }
  }
}