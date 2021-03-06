package view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import model.Point;

public class PlayScene {

    private AnchorPane    anchorPane;                   // main pane of PlayScene
    private String        mapPath;
    private Image         mapImage;
    private int           row, col;
    private MyImgView[][] board;                        // board[][] with images & its numbers
    private MyImgView     blankPiece;                   // my blank piece (w/o image)
    private int           size, freeSpaceX, freeSpaceY;

    //==========================================
    protected PlayScene(AnchorPane anchorPane, int row, int col, String mapName) {
        this.anchorPane = anchorPane;
        this.row = row;
        this.col = col;
        mapPath = setMapPath(mapName);
        mapImage = new Image(mapPath);
        board = new MyImgView[row][col];
        //--- check diff=scene-map_size ---
        getFreeSpace(row, mapImage);
        size = getSize(mapImage, row);
        //--- set images on game board ---
        setImageOnBoard(row, col, mapImage);
    }
    //==========================================

    //--- To set mapPath ---
    private String setMapPath(String mapName) {
        if (mapName.matches("[a-zA-Z0-9]+"))
            return mapPath = "/" + mapName + ".png";
        else
            return mapPath = "blank.png";
    }

    //--- Get Image from MyImgView ---
    private ImageView getImage(MyImgView myImgView) {
        return myImgView.getImg();
    }

    //--- Get positions from moveFrom and blank piece ---
    private double[][] getImgPositions(MyImgView moveFrom) {
        double[][] tmpXY = new double[2][2];
        tmpXY[0][0] = getImage(moveFrom).getLayoutX();
        tmpXY[0][1] = getImage(moveFrom).getLayoutY();
        tmpXY[1][0] = getImage(blankPiece).getLayoutX();
        tmpXY[1][1] = getImage(blankPiece).getLayoutY();

        return tmpXY;
    }

    //--- Swap image with blank piece ---
    public void swapImagesWithBlank(MyImgView moveFrom) {
        double[][] tmpPositionXY = getImgPositions(moveFrom);
        moveFrom.getImg().relocate(tmpPositionXY[1][0], tmpPositionXY[1][1]);
        blankPiece.getImg().relocate(tmpPositionXY[0][0], tmpPositionXY[0][1]);

        setBoardShadow();
    }

    //--- Get piece coordinates ---
    private Point getPiecePoint(MyImgView toCheck){
        Point find;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (board[i][j].equals(toCheck)) {
                    find = new Point(i, j);
                    return find;
                }
            }
        }
        return null;
    }

    //--- Move board pieces [x,y - points of blankPiece] ---
    private void boardMove(int i, int j, int x, int y, MyImgView toMove) {
        board[i][j] = blankPiece;
        board[x][y] = toMove;
    }

    //--- Check if move is within my board bounds ---
    private boolean isAvailable(int i, int j) {
        return (i >= 0 && j >= 0 && i < row && j < col);
    }

    //--- Check if piece can be moved, so it's next to free space 'blank' ---
    private boolean isNextToBlank(int i, int j, MyImgView toCheck) {
        if (isAvailable(i - 1, j))
            if (board[i - 1][j].equals(blankPiece)) {
                boardMove(i, j, i - 1, j, toCheck);
                return true;
            }
        if (isAvailable(i + 1, j))
            if (board[i + 1][j].equals(blankPiece)) {
                boardMove(i, j, i + 1, j, toCheck);
                return true;
            }
        if (isAvailable(i, j - 1))
            if (board[i][j - 1].equals(blankPiece)) {
                boardMove(i, j, i, j - 1, toCheck);
                return true;
            }
        if (isAvailable(i, j + 1))
            if (board[i][j + 1].equals(blankPiece)) {
                boardMove(i, j, i, j + 1, toCheck);
                return true;
            }
        return false;
    }

    //--- CHECKS whether we can move clicked piece ---
    public boolean isMovable(MyImgView toCheck) {
        if (blankPiece.equals(toCheck)) //if blank is clicked
            return false;
        else {
            Point point = getPiecePoint(toCheck);
            if (point!=null)
                return isNextToBlank(point.getX(), point.getY(), toCheck);
            else
                return false;
        }
    }

    //--- SHUFFLING PROCESS --
    public void shufflingMethod(Point move) {
        MyImgView movingPiece = getBoardPiece(move.getX(), move.getY());
        Point blank = getPiecePoint(blankPiece);
        // swap pieces on board
        swapImagesWithBlank(movingPiece);
        boardMove(move.getX(), move.getY(), blank.getX(), blank.getY(), movingPiece);
    }

    //--- Get actual state of the board ---
    public int[][] getBoardState() {
        int[][] state = new int[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                state[i][j] = board[i][j].getPosition();
            }
        }
        return state;
    }

    //--- Add ImageView to my board ---
    private void setImageOnBoard(int row, int col, Image imgToCut) {
        int positionOnBoard = 1;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (i == 0 && j == 0) {//blank piece
                    board[i][j] = setBoardPieces(getBlankImgPiece(), 0);
                    blankPiece = board[i][j];
                } else
                    board[i][j] = setBoardPieces(
                            getPiece(imgToCut, j, i), positionOnBoard++
                    );

            }
        }
    }

    //--- Get MyImgView from board[x,y] ---
    public MyImgView getBoardPiece(int row, int col) {
        return board[row][col];
    }

    //--- Set image on board with specified position number ---
    private MyImgView setBoardPieces(ImageView img, int positionOnBoard) {
        return new MyImgView(img, positionOnBoard);
    }

    //--- Set specified piece of image to ImageView (ViewPort) and return it ---
    private ImageView getPiece(Image img, int from, int to) {
        ImageView imgPiece = new ImageView(img);
        Rectangle2D rectangle2D = new Rectangle2D(from * size, to * size, size, size);
        imgPiece.setViewport(rectangle2D);

        return imgPiece;
    }

    private ImageView getBlankImgPiece() {
        ImageView imgBlank = null;
        try {
            imgBlank = new ImageView(
                    new Image("/blank.png", size, size, false, true)
            );
            imgBlank.setStyle("-fx-effect: innershadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 5, 5)");
            imgBlank.setOpacity(0.85);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgBlank;
    }

    //=== Shadow Effects ===
    private void setShadowDrop(ImageView img) {
        img.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 5, 5)");
    }

    private void setShadowDown(ImageView img) {
        img.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.0, 0, 5)");

    }

    private void setShadowRight(ImageView img) {
        img.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 5, 0)");

    }

    private void setInnerShadow(ImageView img) {
        img.setStyle("-fx-effect: innershadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 5, 5)");
        img.setOpacity(0.85);
    }

    private void clearShadowDrop(ImageView img) {
        img.setStyle("-fx-effect: null");
    }

    //--- Set shadow effects on board ---
    private void setBoardShadow() {
        for (int i = 0; i < row; i++) {
            if (i < row - 1) {
                for (int j = 0; j < col; j++) {
                    if (board[i][j].getPosition() != 0) {
                        if (j < col - 1)
                            clearShadowDrop(board[i][j].getImg());
                        else if (j==col-1)
                            setShadowRight(board[i][j].getImg());
                    }
                    else
                        setInnerShadow(board[i][j].getImg());
                }
            } else if (i == row-1){
                for (int j = 0; j < col-1; j++) {
                    if (board[i][j].getPosition() != 0)
                        setShadowDown(board[i][j].getImg());
                    else
                        setInnerShadow(board[i][j].getImg());
                }
                if (board[row-1][col-1].getPosition() != 0)
                    setShadowDrop(board[row-1][col-1].getImg());
            }
        }
    }

    //--- Add images to my AnchorPane ---
    public void addBoardToPane() {
        double positionY = 0.0;

        for (int i = 0; i < row; i++) {
            double positionX = 0.0;
            for (int j = 0; j < col; j++) {
                anchorPane.getChildren().add(board[i][j].getImg());
                setBoardPiecesOnPane(board[i][j].getImg(), positionX, positionY);
                positionX += size + 1;
            }
            positionY += size + 1;
        }
        setBoardShadow();
    }

    private void setBoardPiecesOnPane(ImageView toMove, double positionX, double positionY) {
        toMove.setLayoutX(positionX + freeSpaceX / 2);
        toMove.setLayoutY(positionY + freeSpaceY / 2);
    }

    //--- Calculate space left on Scene ---
    private void getFreeSpace(int row, Image mapImage) {
        freeSpaceX = (int) anchorPane.getWidth() - (row + (int) mapImage.getWidth());
        freeSpaceY = (int) anchorPane.getHeight() - (row + (int) mapImage.getHeight());
    }

    private int getSize(Image img, int row) {
        return ((int) img.getHeight()) / row;
    }

    //--- Clear Anchor ---
    protected void clearAnchor() {
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                anchorPane.getChildren().remove(board[i][j].getImg());
    }
}
