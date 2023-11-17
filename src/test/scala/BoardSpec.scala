import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import snakes.model.Board

class BoardSpec extends AnyWordSpec with Matchers {

  "A Board" should {
    "be initialized with the correct size and approximate number of features" in {
      val board = Board(10)
      val totalFeatures = board.snakes.size + board.ladders.size
      totalFeatures should be >= 1
      totalFeatures should be <= 3
    }

    "have snakes and ladders within board boundaries" in {
      val board = Board(10)
      board.snakes.keys.foreach(_ should be <= 10)
      board.snakes.values.foreach(_ should be >= 1)
      board.ladders.keys.foreach(_ should be <= 10)
      board.ladders.values.foreach(_ should be <= 10)
    }

    "not have overlapping snakes and ladders" in {
      val board = Board(10)
      board.snakes.keys.foreach(s => board.ladders should not contain key(s))
      board.ladders.values.foreach(l => board.snakes should not contain value(l))
    }

    "handle edge cases like minimum size and no features" in {
      val smallBoard = Board(1)
      smallBoard.snakes shouldBe empty
      smallBoard.ladders shouldBe empty
    }
  }
}
