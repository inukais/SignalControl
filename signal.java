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

	public void changeStatus() {
		// cycle等を検討し、現示を変える必要があるか判断
		switch (status){
			case 0: //clearance
				if(presentLength>=clearance){ //第１現示にする
					status=1;
					presentLength=0;
				}
			case 1: //第１現示
				if(presentLength>= cycle*split0-clearance){
					status=0;
					presentLength=0;
				}
			case 2:

			case 3:

		}
		// if pij>beta or pji>beta then suggestOffset


		//最後にインクリメント（0に戻ってもインクリメント）
		this.presentLength++;
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

