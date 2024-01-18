package snakes.aview

import snakes.controller.ControllerInterface
import snakes.util.{Event, Observer}

import scala.math.sqrt
import java.awt.Color
import javax.swing.{BorderFactory, ImageIcon}
import scala.swing.*

// main frame
class GUI(controller: ControllerInterface) extends Frame with Observer {
  controller.add(this)
  // menubar with AddPlayer and Exit
  title = "Snakes and Ladders"
  menuBar = new MenuBar {
    contents += new Menu("Game") {
      contents += new MenuItem(Action("Add Player") {
        val playerName = Dialog.showInput[String](null,
          null, "Enter Player Name", Dialog.Message.Plain, Swing.EmptyIcon, Nil,"")
        playerName match {
          case Some(name) =>
            if(name.equals("")) {
              Dialog.showMessage(null, "No Player Added!", "Error", Dialog.Message.Plain, Swing.EmptyIcon)
            } else {
              val addString = name + " has been added to the game!"
              Dialog.showMessage(null, addString,"Player Added", Dialog.Message.Plain, Swing.EmptyIcon)
              controller.addPlayer(name)
            }
          case None =>
        }
      })
      contents += new MenuItem(Action("Save") {
        controller.saveGame()
      })
      contents += new MenuItem(Action("Load") {
        controller.loadGame()
      })
      contents += new MenuItem(Action("Exit") {
        controller.exitGame()
      })
    }
  }
  lazy val boardSizeLabel = new FlowPanel {
    contents += new Label("Select Board Size") {
      font = new Font("SansSerif", 3, 16)
    }
  }
  lazy val fourButton = new BorderPanel {
    add(new Button(Action("4x4") {
      controller.createGame(4)
    }), BorderPanel.Position.Center)
    preferredSize = new Dimension(175, 50)
  }
  lazy val sixButton = new BorderPanel {
    add(new Button(Action("6x6") {
      controller.createGame(6)
    }), BorderPanel.Position.Center)
    preferredSize = new Dimension(175, 50)
  }
  lazy val eightButton = new BorderPanel {
    add(new Button(Action("8x8") {
      controller.createGame(8)
    }), BorderPanel.Position.Center)
    preferredSize = new Dimension(175, 50)
  }
  lazy val tenButton = new BorderPanel {
    add(new Button(Action("10x10") {
      controller.createGame(10)
    }), BorderPanel.Position.Center)
    preferredSize = new Dimension(175, 50)
  }
  lazy val startButton: Button = new Button {
    action = Action("Start") {
      undoButton.visible = true
      rollButton.visible = true
      rollResultLabel.visible = true
      startButton.visible = false
      boardSizeLabel.visible = false
      fourButton.visible = false
      sixButton.visible = false
      eightButton.visible = false
      tenButton.visible = false


      controller.startGame()
    }
    visible = true
  }
  lazy val undoButton: Button = new Button {
    action = Action("Undo") {
      controller.undoLastAction()
    }
    visible = false
  }

  lazy val rollButton: Button = new Button {
    action = Action("Roll") {
      if (controller.getCurrentGameState.getPlayers.isEmpty) {
        Dialog.showMessage(null, "Add Players first!", "Error", Dialog.Message.Plain, Swing.EmptyIcon)
      } else if (controller.getCurrentGameState.getPlayers.last.getPosition == controller.getCurrentGameState.getBoard.getSize) {
        Dialog.showMessage(null, controller.getCurrentGameState.getPlayers.last.getName + " has won the game!", "Winner", Dialog.Message.Plain, Swing.EmptyIcon)
      } else {
        controller.rollDice()
      }
    }
    visible = false
  }
  private val rollResultLabel = new Button(""){
    visible = false
  }

  contents = updateContents()
  pack()
  centerOnScreen()
  open()

  private def updateContents() = {
    new BorderPanel {
      add(new FlowPanel {
        contents += new SizeOptionPanel(controller)
      }, BorderPanel.Position.North)

      add(new FieldGridPanel(controller), BorderPanel.Position.Center)
      add(new PlayerPanel(controller), BorderPanel.Position.West)
      add(new FlowPanel {
        contents += new ControlPanel(controller)
      }, BorderPanel.Position.South)
    }
  }

  // Player Panel
  private class PlayerPanel(controller: ControllerInterface) extends BorderPanel {
    // Create a label for displaying text
    private val playerInfoText = new BoxPanel(Orientation.Vertical) {
      contents += new Label(" Players") {
        font = new Font("SansSerif", 3, 20)
      }
      contents += new Label("")
      contents += new Label("  next up:") {
        font = new Font("SansSerif", 3, 16)
      }
    }
    layout(playerInfoText) = BorderPanel.Position.North

    private val playersContainer = new BoxPanel(Orientation.Vertical)
    // Add other components if needed, e.g., buttons, images, etc.
    controller.getCurrentGameState.getPlayers.foreach { element =>
      val playerLayout = new FlowPanel {
        contents += new Button(element.getName + ":" + element.getPosition) {
          preferredSize = new Dimension(100,75)
        }
        contents += new DotPanel(element.getColor)
      }
      playersContainer.contents += playerLayout
    }
    layout(playersContainer) = BorderPanel.Position.Center
    preferredSize = new Dimension(110,300)
  }

  // Panel for creating the Game Size
  class SizeOptionPanel(controller: ControllerInterface) extends BoxPanel(Orientation.Vertical) {
    contents += boardSizeLabel
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += fourButton
      contents += sixButton
      contents += eightButton
      contents += tenButton
    }
  }

  private class FieldGridPanel(controller: ControllerInterface) extends GridPanel(sqrt(controller.getCurrentGameState.getBoard.getSize).toInt, sqrt(controller.getCurrentGameState.getBoard.getSize).toInt) {
    val boardSize = sqrt(controller.getCurrentGameState.getBoard.getSize).toInt
    //zigzag, starting from bottom
    for (row <- 0 until boardSize) {
      val base = (boardSize - row - 1) * boardSize
      if (row % 2 == 0) {
        //right to left
        for (col <- (0 until boardSize).reverse) {
          val fieldNumber = base + col + 1
          contents += new FieldPanel(controller, fieldNumber)
        }
      } else {
        //left to right
        for (col <- 0 until boardSize) {
          val fieldNumber = base + col + 1
          contents += new FieldPanel(controller, fieldNumber)
        }
      }
    }
  }

  // creating a single field (field number, players on the field, and)
  private class FieldPanel(controller: ControllerInterface, field: Int) extends BorderPanel {
    // field number and adds it to the bottom of the field
    private val label = new Label(field.toString)
    layout(label) = BorderPanel.Position.South

    // loops through player queue and adds a dot if player position == field position
    val playerDots = new BoxPanel(Orientation.Horizontal) {
      controller.getCurrentGameState.getPlayers.foreach { element =>
        if (element.getPosition == field) {
          contents += new DotPanel(element.getColor)
        }
      }
    }
    layout(playerDots) = BorderPanel.Position.Center

    // Icons for Snakes and Ladders
    // Load and scale Snake and Ladder Icons
    private val snakeImage = new ImageIcon("SnakeIcon.png").getImage.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH)
    private val ladderImage = new ImageIcon("ladderIcon.png").getImage.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH)
    // Create Labels for displaying icons (icons need to be encapsulated so that they can be added to a field)
    private val snakeLabel = new Label {
      icon = new ImageIcon(snakeImage)
    }
    private val ladderLabel = new Label {
      icon = new ImageIcon(ladderImage)
    }
    private val emptyLabel = new Label {
      preferredSize = new Dimension(25,25)
    }
    // adds image icons to bottom of field by checking if field contains a snake or ladder
    if (controller.getCurrentGameState.getBoard.getSnakes.contains(field)) {
      layout(snakeLabel) = BorderPanel.Position.North
    } else if (controller.getCurrentGameState.getBoard.getLadders.contains(field)) {
      layout(ladderLabel) = BorderPanel.Position.North
    } else {
      layout(emptyLabel) = BorderPanel.Position.North
    }

    // sets preferred size of field and adds a border around the field
    preferredSize = new Dimension(60, 60)
    border = BorderFactory.createLineBorder(Color.BLACK, 1)
  }

  private class DotPanel(dotColor: Color) extends Panel {
    // Set the preferred size for the DotPanel
    preferredSize = new Dimension(20, 20)

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)

      // Set the color for the dot
      g.setColor(dotColor)

      // Calculate the position and size of the dot
      val dotSize = Math.min(size.width, size.height)
      val dotX = (size.width - dotSize) / 2
      val dotY = (size.height - dotSize) / 2

      // Draw the dot
      g.fillOval(dotX, dotY, dotSize, dotSize)
    }
  }

  private class ControlPanel(controller: ControllerInterface) extends BoxPanel(Orientation.Vertical) {
    contents += new BorderPanel {
      layout(startButton) = BorderPanel.Position.Center
      preferredSize = new Dimension(400, 50)
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new BorderPanel {
        layout(rollButton) = BorderPanel.Position.Center
        preferredSize = new Dimension(400, 50)
      }
      contents += new BorderPanel {
        layout(rollResultLabel) = BorderPanel.Position.Center
        preferredSize = new Dimension(50, 50)
      }
      contents += new BorderPanel {
        layout(undoButton) = BorderPanel.Position.Center
        preferredSize = new Dimension(300, 50)
      }
    }
  }

  def update(e: Event): Unit = {
    e match {
      case Event.Create | Event.AddPlayer | Event.Undo | Event.Load =>
        contents = updateContents()
        repaint()
      case Event.Roll(rollResult) =>
        rollResultLabel.text = s"$rollResult"
        contents = updateContents()
        repaint()
      case Event.Start =>
        Dialog.showMessage(contents.head, "Game started, please roll the dice.", title = "Game Started")
        contents = updateContents()
        repaint()
      case Event.Save =>
        Dialog.showMessage(contents.head, "Game saved successfully.", title = "Game Saved")
    }
  }

}