import java.util.*;

class Car {
	int id, speed = 1;
	int[] dep = new int[2];
	int[] arr = new int[2];
	int[] position = new int[2];
	String direction; // どちらの方向に進んでいるか
	int[][] route = new int[6][];
	int phase = 0; // 経路の何段階目か
	int genStep = 0; // 何step目に生成されるか
	boolean isGenerated = false;
	boolean isArrived = false;
	int PRB0 = 5;//左端
	int PRB1 = 3;//右端
	int PRB2 = 1;//上下端
	int PRB = PRB0+PRB1+PRB2;

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
	int isInSec() {
		if(position[1]==20 && position[0]==20) return 0;
		else if(position[1]==20 && position[0]==40) return 1;
		else if(position[1]==20 && position[0]==60) return 2;
		else return -1;
	}

	// 目的地に向かって自動で進む
	// 1stepだけで出来ることをやる
	ReturnValue go() {
		ReturnValue v = new ReturnValue();

		// nextX, nextYは次に向かうべき交差点の場所
		int nextX = route[phase+1][0], nextY = route[phase+1][1];

		if      (position[0] < nextX){ this.direction = v.direction="right"; right();}
		else if (position[0] > nextX){ this.direction = v.direction="left";  left();}
		else if (position[1] < nextY){ this.direction = v.direction="up";    up();  }
		else if (position[1] > nextY){ this.direction = v.direction="down";  down();}
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
		else System.out.println("something wrong. ## car.java #1");

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
		int p1,p2,temp;

		temp = r.nextInt(PRB);
		if(temp<PRB0) p1 = 0; //左端が起点
		else if(temp<PRB0+PRB1) p1 = 1; //右端が起点
		else p1 = r.nextInt(6)+2; //上下が起点。2~7の範囲で

		do p2 = r.nextInt(8); while (p1 == p2);
		this.dep = this.position = point[p1];
		this.arr = point[p2];
		//System.out.printf("gen %d,%d\n",p1,p2);

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

