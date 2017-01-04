import java.io.*;
import java.util.*;

class Simulation {

	public static int MAXSTEP = 100;
	public static int MAXCAR = 100;
	public static int NPS = 5; //毎秒何台生成するか
	public static boolean PRINT_CAR = true;
	public static boolean PRINT_SIG = false;

	public static void main(String args[]){

		// 信号の生成
		Signal[] sig = new Signal[3];
		sig[0] = new Signal(0, 20, 20);
		sig[1] = new Signal(1, 40, 20);
		sig[2] = new Signal(2, 60, 20);

		// 車の生成
		Car[] c = new Car[MAXCAR];
		for(int i=0; i<MAXCAR; i++)
			c[i] = new Car(i, i/NPS); // 第二引数：何step目に生成されるか

		// セルに車がいるか情報を取り扱う配列
		Cell[][] cell = new Cell[81][];
		for(int i=0; i<=80; i++){
			cell[i] = new Cell[41];
			for(int j=0; j<=40; j++){
				cell[i][j] = new Cell(i,j);
			}
		}

		// 1stepごとに進めていく
		for(int step=0; step<MAXSTEP; step++) {
			doCarStep(c, sig, cell, step);
			doSignalStep(sig, step);
		}
	}

	public static void doCarStep(Car[] c, Signal[] sig, Cell[][] cell, int step) {

		// 車を動かす
		for(int i=0; i<MAXCAR; i++){

			//今回のステップで生成されることになっている車を動かし始める
			if(step == c[i].genStep) c[i].isGenerated = true;

			// 到着済み or 未生成の車は無視
			if(c[i].isArrived || !c[i].isGenerated) continue;

			// 車が次に行くセルを取得
			ReturnValue v = c[i].goCheck();

			// 本当に進んでよいか確認 (次のセルに車が居ないか)
			boolean flag = ! cell[v.x][v.y].existing.contains(v.direction);

			// 本当に進んでよいか確認 (信号が赤でないか)
			int sigNum = c[i].isInSec();
			if(sigNum>-1)
				// isAllowed()の引数は，これまでの進行方向，これからの進行方向
				flag = flag && sig[sigNum].isAllowed(c[i].direction, v.direction);

			//System.out.printf("%d->[%d,%d]:%s\n", i, v.x, v.y, flag?"true":"false");
			if(flag) {
				if(sigNum>-1) c[i].expedSec=true; //交差点に居るのであれば，発進前に交差点通過済フラグ立てる
				v = c[i].go();
				cell[v.x][v.y].nextExisting.add(v.direction); //車の存在情報を登録
			} else {
				// 停車時間の測定
				c[i].waitingLength2++;
				if(!c[i].expedSec) c[i].waitingLength1++;
			}
		}

		// 車の現在位置を出力する
		if (PRINT_CAR) {
			System.out.printf("======== step %4d ========\n", step);
			for(int i=0; i<MAXCAR; i++)
				c[i].printLocation();
			System.out.println("");
		}
		// cellのnextExistingをexistingに移行
		for(int i=0; i<=80; i++){
			for(int j=0; j<=40; j++){
				cell[i][j].existing = cell[i][j].nextExisting;
				cell[i][j].nextExisting.clear();
			}
		}
	}
	// end doCarStep()

	public static void doSignalStep(Signal[] sig, int step) {
		// 一定条件下でオフセット制御を提案する
		// 現示を切り替える

		for (int i=0; i<3; i++){
			sig[i].changeStatus(); //信号ごとのstep毎のルーチンワーク
		}

		// 信号の現在状態を出力する
		if (PRINT_SIG) {
			System.out.printf("======== step %4d ========\n", step);
			for(int i=0; i<3; i++)
				sig[i].printStatus();
			System.out.println("");
		}
	}
	// end doSignalStep()

}

class ReturnValue {
	int x, y;
	String direction;
}
class Cell {
	int x, y;
	List<String> existing = new ArrayList<String>();
	List<String> nextExisting = new ArrayList<String>();

	public Cell(int x,int y){
		this.x = x;
		this.y = y;
	}

	/*
	void up()   { existing.add("up");    }
	void down() { existing.add("down");  }
	void left() { existing.add("left");  }
	void right(){ existing.add("right"); }

	void upDel()   { existing.remove("up");    }
	void downDel() { existing.remove("down");  }
	void leftDel() { existing.remove("left");  }
	void rightDel(){ existing.remove("right"); }

	boolean upE()   { return existing.contains("up");    }
	boolean downE() { return existing.contains("down");  }
	boolean leftE() { return existing.contains("left");  }
	boolean rightE(){ return existing.contains("right"); }
	*/

}
