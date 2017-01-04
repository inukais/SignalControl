import java.util.*;

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

	/*
		status
		0:clearance
		1:第1現示
		2:clearance
		3:先出し右折
		4:第2現示
		5:後出し右折
	*/
	int status=0; // 現示
	int presentLength=0; // 今の現示が何step続いているか
	int id;
	int[] position = new int[2];
	int[] rightTurnLane = new int[2];
	int mode = 1; //1:独立、2:起点、3:従属
	//従属モードにおける起点の信号機の番号。独立or起点のときは-1
	int parent = -1;
	boolean isRightMode = false; //右折現示があるか

	int[] time = new int[6];

	public Signal(int id, int x, int y) {
		this.id = id;
		this.position[0] = x;
		this.position[1] = y;
		this.rightTurnLane[0] = this.rightTurnLane[1] = 0;

		// 独立モードの現示維持時間(右折なし)
		time[0] = clearance;
		time[1] = (int)(cycle*split0)-clearance;
		time[2] = clearance;
		time[3] = 0;
		time[4] = (int)(cycle*split1)-clearance;
		time[5] = 0;
	}

	// サイクル毎のルーチンワーク
	public void changeStatus() {
		// cycle等を検討し、現示を変える必要があるか判断
		if(presentLength >= time[status]){
			do { status++; if(status==6) status=0; } while(time[status]==0);
			presentLength=0;
		}

		if(status==0) reconsider();

		// if pij>beta or pji>beta then suggestOffset

		//最後にインクリメント（0に戻ってもインクリメント）
		this.presentLength++;
	}

	public void reconsider(){
		// 振り出しに戻った(status==0)のでサイクル長の見直しをする

		double lambda = 0.0; // 交差点飽和度：暫定
		cycle = (int)((1.5 * clearance + 5.0) / (1.0-lambda));

		// 独立モードの現示維持時間(右折なし)
		time[0] = clearance;
		time[1] = (int)(cycle*split0)-clearance;
		time[2] = clearance;
		time[3] = 0;
		time[4] = (int)(cycle*split1)-clearance;
		time[5] = 0;
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

	// 車に，進んでいいか教えてあげる
	public boolean isAllowed(String nowDirection, String nextDirection){

		// 第2現示で進む車 (右折現示あり)
		if(nowDirection == "left"){
			if(isRightMode && nextDirection == "up")
				{ if(status==3 || status==5) return true; else return false; }
			else
				{ if(status==4) return true; else return false; }
		}
		else if(nowDirection == "right"){
			if(isRightMode && nextDirection == "down")
				{ if(status==3 || status==5) return true; else return false; }
			else
				{ if(status==4) return true; else return false; }
		}

		// 第1現示で進む車
		else
			{ if(status==1) return true; else return false; }
	}

}

