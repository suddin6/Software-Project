import java.util.Scanner;

public class Vote{
    public static void main(String[] args) {
        Scanner Candidate = new Scanner(System.in);
        System.out.print("Please insert name of Canddidate you wish to vote for: ");
            while(true){
                if (Candidate == Candidate_id){
                    String name = Candidate.nextLine();
                    System.out.println("You have voted for " + name);
                    votes_candidate_fk = votes_candidate_fk + 1;
                    break;
                }
                else{
                    System.out.println("Invalid candidate, please try again.");

                }

            }
        

    }

}