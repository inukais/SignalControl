import java.io.*;
import java.util.*;

class Simulation {
	public static void main(String args[]){

		int maxStep = 100, maxCar = 1000;

		// 座標を指定して生成
		Signal sig0 = new Signal(20, 20);
		Signal sig1 = new Signal(40, 20);
		Signal sig2 = new Signal(60, 20);

		Car[] c = new Car[maxCar];
		for(int i=0; i<maxCar; i++)
			c[i] = new Car(i);

		// 1stepごとに進めていく
		for(int step=0; step<maxStep; step++) {

			// 車を動かす
			for(int i=0; i<maxCar; i++){
				if(!c[i].isArrived) c[i].go();
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
	void right() { position[0] += speed; }
	void left()  { position[0] -= speed; }
	void up()    { position[1] += speed; }
	void down()  { position[1] -= speed; }

	// 目的地に向かって自動で進む
	// 1stepだけで出来ることをやる
	void go() {

		int nextX = route[phase+1][0], nextY = route[phase+1][1];

		if      (position[0] < nextX) right();
		else if (position[0] > nextX) left();
		else if (position[1] < nextY) up();
		else if (position[1] > nextY) down();
		else System.out.println("something wrong");

		if (route[phase+1][0] == position[0] && route[phase+1][1] == position[1]) phase++;
		if (arr[0] == position[0] && arr[1] == position[1]) isArrived = true;
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
		System.out.printf("%d's pos: %d,%d | ", id, position[0], position[1]);
	}

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

