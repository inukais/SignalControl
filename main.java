import java.io.*;
import java.util.*;

class Simulation {
	public static void main(String args[]){

		int maxStep = 20, maxCar = 30;

		// 信号の生成
		Signal sig0 = new Signal(20, 20);
		Signal sig1 = new Signal(40, 20);
		Signal sig2 = new Signal(60, 20);

		// 車の生成
		Car[] c = new Car[maxCar];
		for(int i=0; i<maxCar; i++)
			c[i] = new Car(i);

		// セルに車がいるか情報を取り扱う配列
		Cell[][] cell = new Cell[81][];
		for(int i=0; i<=80; i++){
			cell[i] = new Cell[41];
			for(int j=0; j<=40; j++){
				cell[i][j] = new Cell(i,j);
			}
		}

		// 1stepごとに進めていく
		for(int step=0; step<maxStep; step++) {

			// 車を動かす
			for(int i=0; i<maxCar; i++){

				// 到着済みの車は無視
				if(c[i].isArrived) continue;

				// 車が次に行くセルを取得
				ReturnValue v = c[i].goCheck();

				// 本当に進んでよいか確認
				boolean flag = ! cell[v.x][v.y].existing.contains(v.direction);
				//System.out.printf("%d->[%d,%d]:%s\n", i, v.x, v.y, flag?"true":"false");
				if(flag) c[i].go();
			}

			// 車の現在位置を出力する
			System.out.printf("======== step %d ========\n", step);
			for(int i=0; i<maxCar; i++){
				c[i].printLocation();
			}
			System.out.println("");

			// 到着した車を消滅させる
			// for(int i=0; i<maxCar; i++){
			//	if (c[i].isArrived) c[i] = null;
			// }

			//try{Thread.sleep(50);} catch(Exception e){}
			//System.out.printf("\033[2J");
		}

		//try {c[0].printLocation();}
		//catch(NullPointerException e) {System.out.printf("already arrived\n");}
	}
}

class Car {
	int id, speed = 1;
	int[] dep = new int[2];
	int[] arr = new int[2];
	int[] position = new int[2];
	int[][] route = new int[6][];
	int phase = 0; // 経路の何段階目か
	boolean isArrived = false;

	public Car(int id) {
		this.id = id;
		this.genRoute();
	}

	// 次のセルの車の存否を確かめた上で進行
	void right() { position[0] += speed; }
	void left()  { position[0] -= speed; }
	void up()    { position[1] += speed; }
	void down()  { position[1] -= speed; }

	// 目的地に向かって自動で進む
	// 1stepだけで出来ることをやる
	void go() {

		// nextX, nextYは次に向かうべき交差点の場所
		int nextX = route[phase+1][0], nextY = route[phase+1][1];

		if      (position[0] < nextX) right();
		else if (position[0] > nextX) left();
		else if (position[1] < nextY) up();
		else if (position[1] > nextY) down();
		else System.out.println("something wrong");

		// 交差点に達したらphaseをカウントアップ
		if (route[phase+1][0] == position[0] && route[phase+1][1] == position[1]) phase++;
		if (arr[0] == position[0] && arr[1] == position[1]) isArrived = true;
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

		//System.out.printf("dep: %d,%d arr: %d,%d\n",
		//dep[0], dep[1], arr[0], arr[1] );

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
		// ルートの生成終了

	}

	void printLocation() {
		System.out.printf("car:%d pos:%d,%d\n", id, position[0], position[1]);
	}
}
class ReturnValue {
	int x, y;
	String direction;
}
class Cell {
	int x, y;
	List<String> existing = new ArrayList<String>();

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
	int split0, split1, mode, status;
	int[] position = new int[2];
	int[] rightTurnLane = new int[2];

	public Signal(int x, int y) {
		this.position[0] = x;
		this.position[1] = y;
		this.rightTurnLane[0] = this.rightTurnLane[1] = 0;
	}
}

