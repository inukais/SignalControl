import java.io.*;
import java.util.*;

class Simulation {

	public static int MAXSTEP = 100;
	public static int MAXCAR = 100;
	public static int NPS = 5; //毎秒何台生成するか
	public static boolean PRINT_CAR = false;
	public static boolean PRINT_SIG = true;

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
			doCarStep(c, cell, step);
			doSignalStep(sig, step);
		}
	}

	public static void doCarStep(Car[] c, Cell[][] cell, int step) {

		// 車を動かす
		for(int i=0; i<MAXCAR; i++){

			//今回のステップで生成されることになっている車を動かし始める
			if(step == c[i].genStep) c[i].isGenerated = true;

			// 到着済み or 未生成の車は無視
			if(c[i].isArrived || !c[i].isGenerated) continue;

			// 車が次に行くセルを取得
			ReturnValue v = c[i].goCheck();

			// 本当に進んでよいか確認
			boolean flag = ! cell[v.x][v.y].existing.contains(v.direction);
			//System.out.printf("%d->[%d,%d]:%s\n", i, v.x, v.y, flag?"true":"false");
			//信号が赤等の事情もflagに加味する
			if(flag) {
				if(c[i].isInSec()) c[i].expedSec=true; //発進前に交差点通過済フラグ立てる
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

		//try{Thread.sleep(50);} catch(Exception e){}
		//System.out.printf("\033[2J");
	}
	// end doCarStep()

	public static void doSignalStep(Signal[] sig, int step) {
		// 一定条件下でオフセット制御を提案する
		// 現示を切り替える

		for (int i=0; i<3; i++){
			// 現示に変更がなければincrement
			if (sig[i].changeStatus())
				sig[i].presentLength++;
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

class Car {
	int id, speed = 1;
	int[] dep = new int[2];
	int[] arr = new int[2];
	int[] position = new int[2];
	int[][] route = new int[6][];
	int phase = 0; // 経路の何段階目か
	int genStep = 0; // 何step目に生成されるか
	boolean isGenerated = false;
	boolean isArrived = false;

	// 測定に用いる変数
	boolean expedSec = false; //1つ目の交差点を通過したか
	int waitingLength1 = 0; // 1つ目の交差点を通過するまでの停車時間
	int waitingLength2 = 0; // 目的地に到着するまでの停車時間

	public Car(int id, int genStep) {
		this.id = id;
		this.genStep = genStep;
		this.genRoute();
	}

	// 次のセルの車の存否を確かめた上で進行
	void right() { position[0] += speed; }
	void left()  { position[0] -= speed; }
	void up()    { position[1] += speed; }
	void down()  { position[1] -= speed; }

	// 交差点に居るか否かの判定
	boolean isInSec() {
		if(position[1]==20 && (position[0]==20 || position[0]==40 || position[0]==60))
			return true;
		else	return false;
	}

	// 目的地に向かって自動で進む
	// 1stepだけで出来ることをやる
	ReturnValue go() {
		ReturnValue v = new ReturnValue();

		// nextX, nextYは次に向かうべき交差点の場所
		int nextX = route[phase+1][0], nextY = route[phase+1][1];

		if      (position[0] < nextX){ v.direction="right"; right();}
		else if (position[0] > nextX){ v.direction="left";  left();}
		else if (position[1] < nextY){ v.direction="up";    up();  }
		else if (position[1] > nextY){ v.direction="down";  down();}
		else System.out.println("something wrong");

		// 交差点に達したらphaseをカウントアップ
		if (route[phase+1][0] == position[0] && route[phase+1][1] == position[1]) phase++;
		if (arr[0] == position[0] && arr[1] == position[1]) isArrived = true;

		v.x = position[0];
		v.y = position[1];

		return v;
	}

	ReturnValue goCheck() {
		ReturnValue v = new ReturnValue();

		// nextX, nextYは次に向かうべき交差点の場所
		int nextX = route[phase+1][0], nextY = route[phase+1][1];

		if (position[0] == nextX){
			v.x = position[0];
			v.y = (position[1] > nextY) ? position[1]-1 : position[1]+1;
			v.direction = (position[1] > nextY) ? "down" : "up";
		}
		else if (position[1] == nextY){
			v.x = (position[0] > nextX) ? position[0]-1 : position[0]+1;
			v.y = position[1];
			v.direction = (position[0] > nextY) ? "left" : "right";
		}
		else System.out.println("something wrong");

		return v;
	}

	void genRoute() {

		int point[][] = {
			// 起終点となりうる場所は次の8つのみ
			{ 0,20}, {80,20},
			{20, 0}, {40, 0},
			{60, 0}, {20,40},
			{40,40}, {60,40}
		};

		// 起点p1と終点p2を決定する(選ぶ)
		Random r = new Random();
		int p1 = r.nextInt(8);
		int p2 = r.nextInt(8);
		while (p1 == p2) p2 = r.nextInt(8);
		this.dep = this.position = point[p1];
		this.arr = point[p2];

		// ルートの生成
		this.route[0] = this.dep;
		int i=1, x, y;
		boolean flag = true;

		while(flag) {
			x = route[i-1][0];
			y = route[i-1][1];
			route[i] = new int[2];

			// 上下に進むだけの場合
			if (x == arr[0]){
				route[i][0] = x;
				route[i][1] = (y < arr[1]) ? y+20 : y-20;
			}
			// 左右に進まなければならない場合
			else if (y==20){
				route[i][0] = (x < arr[0]) ? x+20 : x-20;
				route[i][1] = y;
			} else{
				route[i][0] = x;
				route[i][1] = 20;
			}
			//System.out.printf("route[%d]に%d,%dを追加しました。\n",i,route[i][0],route[i][1]);

			flag = route[i][0]!=arr[0] || route[i][1]!=arr[1];
			i++;
		}
	}
	// genRoute()終了

	void printLocation() {
		System.out.printf("[%3d]:(%2d,%2d,%2d,%2d) ", id, position[0], position[1], waitingLength1, waitingLength2);
		if (id%6 == 5) System.out.println();
	}
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

}

class Signal {
	public double GANMA = 1.5;
	public double DELTA = 1.1;
	public double EPSILON = 0.2;
	public int X = 3; // 右折レーンに溜まれる台数
	public double P = 0.6;
	public double Q = 0.5;
	public double R = 0.3;

	double cap_i = 0.45;
	double alpha = 0.25 * cap_i; // cap_iが変わったら、あわせて変更
	double beta  = 0.125* cap_i;

	// 表2に従い，初期値を代入
	double split0 = 0.5, split1 = 0.5;
	int cycle = 50, clearance = 5;
	int offset = 0;

	// 0:clearance，1:第1現示，2:第2現示，3:第1現示右折，4:第2現示右折
	int status=0;
	int presentLength=0; // 今の現示が何step続いているか
	int id;
	int[] position = new int[2];
	int[] rightTurnLane = new int[2];
	int mode = 1; //1:独立、2:起点、3:従属
	//従属モードにおける起点の信号機の番号。独立or起点のときは-1
	int parent = -1;

	public Signal(int id, int x, int y) {
		this.id = id;
		this.position[0] = x;
		this.position[1] = y;
		this.rightTurnLane[0] = this.rightTurnLane[1] = 0;
	}

	public boolean changeStatus() {
		// cycle等を検討し、現示を変える必要があるか判断
		switch (status){
			case 0: //clearance
				if(presentLength>=clearance){ //第１現示にする
					status=1;
					presentLength=0;
				}
			case 1: //第１現示

			case 2:

			case 3:

		}
		// if pij>beta or pji>beta then suggestOffset


		// 現示が変わったらtrueを返す
		return true;
	}

	public void suggestOffset(int i) {
	}

	// 従属モードになるか決める（(2)式）
	public void considerSuggestion(int step, int e, int cycle, int suggester) {
		if (step<=EPSILON*cycle || step>=(1-EPSILON)*cycle){
			this.mode = 3;
			this.parent = suggester;
		}
	}

	public void printStatus() {
		System.out.printf("[%d] mode:%d, parent:%d, status:%d, presentLength:%d\n", id, mode, parent, status, presentLength);

	}
}

