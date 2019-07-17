package outputs;
import edu.cmu.cs.plural.annot.*;

@ClassStates({@State(name = "alive")})
class Fibonacci {
@Perm(ensures="unique(this) in alive")
Fibonacci() {   }

@Perm(requires="full(this) in alive",
ensures="full(this) in alive")
 public Integer computeFibo(Integer num) {
 return null;
 
} 
@Perm(requires="unique(this) in alive",
ensures="unique(this) in alive")
  void main(String[] args) {
 
} 
@Perm(requires="pure(this) in alive",
ensures="pure(this) in alive")
 public void display(Integer num) {
 
} 

}ENDOFCLASS

