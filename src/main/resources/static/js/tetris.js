class TetrisGame {
    constructor() {
        this.canvas = document.getElementById('gameBoard');
        this.ctx = this.canvas.getContext('2d');
        this.nextCanvas = document.getElementById('nextPiece');
        this.nextCtx = this.nextCanvas.getContext('2d');
        
        // 游戏配置
        this.BOARD_WIDTH = 10;
        this.BOARD_HEIGHT = 20;
        this.BLOCK_SIZE = 30;
        
        // 游戏状态
        this.board = this.createBoard();
        this.currentPiece = null;
        this.nextPiece = null;
        this.score = 0;
        this.level = 1;
        this.lines = 0;
        this.gameRunning = false;
        this.gamePaused = false;
        this.gameLoop = null;
        this.dropTimer = 0;
        this.dropInterval = 1000; // 毫秒
        
        // 方块形状定义
        this.pieces = {
            I: [
                [[1,1,1,1]],
                [[1],[1],[1],[1]]
            ],
            O: [
                [[1,1],[1,1]]
            ],
            T: [
                [[0,1,0],[1,1,1]],
                [[1,0],[1,1],[1,0]],
                [[1,1,1],[0,1,0]],
                [[0,1],[1,1],[0,1]]
            ],
            S: [
                [[0,1,1],[1,1,0]],
                [[1,0],[1,1],[0,1]]
            ],
            Z: [
                [[1,1,0],[0,1,1]],
                [[0,1],[1,1],[1,0]]
            ],
            J: [
                [[1,0,0],[1,1,1]],
                [[1,1],[1,0],[1,0]],
                [[1,1,1],[0,0,1]],
                [[0,1],[0,1],[1,1]]
            ],
            L: [
                [[0,0,1],[1,1,1]],
                [[1,0],[1,0],[1,1]],
                [[1,1,1],[1,0,0]],
                [[1,1],[0,1],[0,1]]
            ]
        };
        
        // 方块颜色
        this.colors = {
            I: '#00f5ff',
            O: '#ffff00',
            T: '#a000ff',
            S: '#00ff00',
            Z: '#ff0000',
            J: '#0000ff',
            L: '#ff8000'
        };
        
        this.initEventListeners();
    }
    
    createBoard() {
        return Array(this.BOARD_HEIGHT).fill().map(() => Array(this.BOARD_WIDTH).fill(0));
    }
    
    initEventListeners() {
        const startBtn = document.getElementById('startBtn');
        const pauseBtn = document.getElementById('pauseBtn');
        const restartBtn = document.getElementById('restartBtn');
        
        startBtn.addEventListener('click', () => this.startGame());
        pauseBtn.addEventListener('click', () => this.togglePause());
        restartBtn.addEventListener('click', () => this.restartGame());
        
        document.addEventListener('keydown', (e) => this.handleKeyPress(e));
    }
    
    startGame() {
        this.gameRunning = true;
        this.gamePaused = false;
        this.board = this.createBoard();
        this.score = 0;
        this.level = 1;
        this.lines = 0;
        this.dropInterval = 1000;
        
        this.currentPiece = this.createPiece();
        this.nextPiece = this.createPiece();
        
        this.updateDisplay();
        this.hideElement('startBtn');
        this.showElement('pauseBtn');
        this.hideElement('gameOver');
        
        this.gameLoop = setInterval(() => this.update(), 50);
    }
    
    togglePause() {
        if (!this.gameRunning) return;
        
        this.gamePaused = !this.gamePaused;
        const pauseBtn = document.getElementById('pauseBtn');
        pauseBtn.textContent = this.gamePaused ? '继续' : '暂停';
    }
    
    restartGame() {
        this.stopGame();
        this.startGame();
    }
    
    stopGame() {
        this.gameRunning = false;
        this.gamePaused = false;
        if (this.gameLoop) {
            clearInterval(this.gameLoop);
            this.gameLoop = null;
        }
        this.showElement('startBtn');
        this.hideElement('pauseBtn');
    }
    
    createPiece() {
        const types = Object.keys(this.pieces);
        const type = types[Math.floor(Math.random() * types.length)];
        return {
            type: type,
            shape: this.pieces[type][0],
            x: Math.floor(this.BOARD_WIDTH / 2) - 1,
            y: 0,
            rotation: 0
        };
    }
    
    update() {
        if (!this.gameRunning || this.gamePaused) return;
        
        this.dropTimer += 50;
        if (this.dropTimer >= this.dropInterval) {
            this.movePiece(0, 1);
            this.dropTimer = 0;
        }
        
        this.draw();
    }
    
    movePiece(dx, dy) {
        const newX = this.currentPiece.x + dx;
        const newY = this.currentPiece.y + dy;
        
        if (this.isValidPosition(this.currentPiece.shape, newX, newY)) {
            this.currentPiece.x = newX;
            this.currentPiece.y = newY;
            return true;
        } else if (dy > 0) {
            // 方块到底了，固定方块
            this.placePiece();
            this.clearLines();
            this.currentPiece = this.nextPiece;
            this.nextPiece = this.createPiece();
            
            // 检查游戏结束
            if (!this.isValidPosition(this.currentPiece.shape, this.currentPiece.x, this.currentPiece.y)) {
                this.gameOver();
            }
        }
        return false;
    }
    
    rotatePiece() {
        const currentType = this.currentPiece.type;
        const rotations = this.pieces[currentType];
        const nextRotation = (this.currentPiece.rotation + 1) % rotations.length;
        const newShape = rotations[nextRotation];
        
        if (this.isValidPosition(newShape, this.currentPiece.x, this.currentPiece.y)) {
            this.currentPiece.shape = newShape;
            this.currentPiece.rotation = nextRotation;
        }
    }
    
    isValidPosition(shape, x, y) {
        for (let row = 0; row < shape.length; row++) {
            for (let col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    const newX = x + col;
                    const newY = y + row;
                    
                    if (newX < 0 || newX >= this.BOARD_WIDTH || 
                        newY >= this.BOARD_HEIGHT ||
                        (newY >= 0 && this.board[newY][newX])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    placePiece() {
        const shape = this.currentPiece.shape;
        const type = this.currentPiece.type;
        
        for (let row = 0; row < shape.length; row++) {
            for (let col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    const x = this.currentPiece.x + col;
                    const y = this.currentPiece.y + row;
                    if (y >= 0) {
                        this.board[y][x] = type;
                    }
                }
            }
        }
    }
    
    clearLines() {
        let linesCleared = 0;
        
        for (let row = this.BOARD_HEIGHT - 1; row >= 0; row--) {
            if (this.board[row].every(cell => cell !== 0)) {
                this.board.splice(row, 1);
                this.board.unshift(Array(this.BOARD_WIDTH).fill(0));
                linesCleared++;
                row++; // 重新检查同一行
            }
        }
        
        if (linesCleared > 0) {
            this.lines += linesCleared;
            this.score += this.calculateScore(linesCleared);
            this.level = Math.floor(this.lines / 10) + 1;
            this.dropInterval = Math.max(100, 1000 - (this.level - 1) * 100);
            this.updateDisplay();
        }
    }
    
    calculateScore(linesCleared) {
        const lineScores = [0, 40, 100, 300, 1200];
        return lineScores[linesCleared] * this.level;
    }
    
    gameOver() {
        this.stopGame();
        document.getElementById('finalScore').textContent = this.score;
        this.showElement('gameOver');
    }
    
    handleKeyPress(e) {
        if (!this.gameRunning || this.gamePaused) return;
        
        switch(e.code) {
            case 'ArrowLeft':
                e.preventDefault();
                this.movePiece(-1, 0);
                break;
            case 'ArrowRight':
                e.preventDefault();
                this.movePiece(1, 0);
                break;
            case 'ArrowDown':
                e.preventDefault();
                this.movePiece(0, 1);
                break;
            case 'Space':
                e.preventDefault();
                this.rotatePiece();
                break;
            case 'KeyP':
                e.preventDefault();
                this.togglePause();
                break;
        }
    }
    
    draw() {
        this.drawBoard();
        this.drawCurrentPiece();
        this.drawNextPiece();
    }
    
    drawBoard() {
        // 清空画布
        this.ctx.fillStyle = '#1a202c';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        // 绘制已放置的方块
        for (let row = 0; row < this.BOARD_HEIGHT; row++) {
            for (let col = 0; col < this.BOARD_WIDTH; col++) {
                if (this.board[row][col]) {
                    this.drawBlock(this.ctx, col, row, this.colors[this.board[row][col]]);
                }
            }
        }
        
        // 绘制网格线
        this.ctx.strokeStyle = '#2d3748';
        this.ctx.lineWidth = 1;
        for (let i = 0; i <= this.BOARD_WIDTH; i++) {
            this.ctx.beginPath();
            this.ctx.moveTo(i * this.BLOCK_SIZE, 0);
            this.ctx.lineTo(i * this.BLOCK_SIZE, this.canvas.height);
            this.ctx.stroke();
        }
        for (let i = 0; i <= this.BOARD_HEIGHT; i++) {
            this.ctx.beginPath();
            this.ctx.moveTo(0, i * this.BLOCK_SIZE);
            this.ctx.lineTo(this.canvas.width, i * this.BLOCK_SIZE);
            this.ctx.stroke();
        }
    }
    
    drawCurrentPiece() {
        if (!this.currentPiece) return;
        
        const shape = this.currentPiece.shape;
        const color = this.colors[this.currentPiece.type];
        
        for (let row = 0; row < shape.length; row++) {
            for (let col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    const x = this.currentPiece.x + col;
                    const y = this.currentPiece.y + row;
                    if (y >= 0) {
                        this.drawBlock(this.ctx, x, y, color);
                    }
                }
            }
        }
    }
    
    drawNextPiece() {
        if (!this.nextPiece) return;
        
        // 清空下一个方块的画布
        this.nextCtx.fillStyle = '#1a202c';
        this.nextCtx.fillRect(0, 0, this.nextCanvas.width, this.nextCanvas.height);
        
        const shape = this.nextPiece.shape;
        const color = this.colors[this.nextPiece.type];
        const blockSize = 20;
        
        // 计算居中位置
        const offsetX = (this.nextCanvas.width - shape[0].length * blockSize) / 2;
        const offsetY = (this.nextCanvas.height - shape.length * blockSize) / 2;
        
        for (let row = 0; row < shape.length; row++) {
            for (let col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    const x = offsetX + col * blockSize;
                    const y = offsetY + row * blockSize;
                    this.drawBlockAt(this.nextCtx, x, y, blockSize, color);
                }
            }
        }
    }
    
    drawBlock(ctx, x, y, color) {
        const pixelX = x * this.BLOCK_SIZE;
        const pixelY = y * this.BLOCK_SIZE;
        this.drawBlockAt(ctx, pixelX, pixelY, this.BLOCK_SIZE, color);
    }
    
    drawBlockAt(ctx, x, y, size, color) {
        // 绘制方块主体
        ctx.fillStyle = color;
        ctx.fillRect(x, y, size, size);
        
        // 绘制高光效果
        ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
        ctx.fillRect(x, y, size, 2);
        ctx.fillRect(x, y, 2, size);
        
        // 绘制阴影效果
        ctx.fillStyle = 'rgba(0, 0, 0, 0.3)';
        ctx.fillRect(x, y + size - 2, size, 2);
        ctx.fillRect(x + size - 2, y, 2, size);
        
        // 绘制边框
        ctx.strokeStyle = 'rgba(0, 0, 0, 0.5)';
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, size, size);
    }
    
    updateDisplay() {
        document.getElementById('score').textContent = this.score;
        document.getElementById('level').textContent = this.level;
        document.getElementById('lines').textContent = this.lines;
    }
    
    showElement(id) {
        document.getElementById(id).classList.remove('hidden');
    }
    
    hideElement(id) {
        document.getElementById(id).classList.add('hidden');
    }
}

// 初始化游戏
document.addEventListener('DOMContentLoaded', () => {
    const game = new TetrisGame();
}); 