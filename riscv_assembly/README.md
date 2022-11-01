:::info
從HackMD推送過來的版本可能有些圖片或是語法會跑掉, 有需要的人可以直接到[我的HackMD](https://hackmd.io/525bBnyDS5G7CeObA9hWYQ?view#AIAS-Spring-2022-LAB-3-RISC-V-Assembly-)閱讀喔！
:::
----
# <center>AIAS Spring 2022<br><font color="#0000dd"> LAB 3 RISC-V Assembly </font></center>
[TOC]

## Introduction
- 在課堂上有介紹了基本的 RISC-V ISA，在 LAB3 中我們將會：
    1. 學習如何利用 Venus 進行組合語言的編寫與除錯。
    2. 學習如何寫RISC-V assembly 以及function calls。
    3. 學習如何利用 qemu進行 assembly code的模擬，並使用 C/Assembly hybrid的方法提升程式執行效率。
    4. 利用 Venus 網站完成 HW3 的費波那契演算法和數獨演算法。
- 如果你不是初學者, 有一定的基礎的話, 你可以試著完成Bonus Homework
<!--
- 完成本次 LAB3 作業後，利用 Gitlab 創建自己的 repository 並繳交作業說明文件，在之後的作業均需要透過此方式進行繳交，可以參考 **Working Environment Setup**和 **Homework Document Template**。 
-->
<!--
## Working Environment Setup

```cmd=
## bring up the AIAS course docker container as described in lab 2
## clone the lab3 files
$  cd /workspace/projects
$  git clone https://github.com/xuann6/AI-Computing-Architecture-and-System.git
$  cd riscv_assembly

## add your private upstream repositories
## make sure you have create project repo under your gitlab account
$  git remote add gitlab https://github.com/<your GitHub account>/AI-Computing-Architecture-and-System.git
```
- Make sure you create your own repo in your GitHub account and set the upstream correctly.
- When you are done with your code, you have to push your code back to your own GitHub account with the following command :
```shell=
## the first time
$  git push --set-upstream https://github.com//AI-Computing-Architecture-and-System.git master

## after the first time
$  git fetch origin master
$  git push gitlab master
```

-->
- lab3中的檔案結構
    - Lab3-3
        - hello_world.c
        - example_1_main.c
        - example_1_sum.S
        - example_2_main.c
        - example_2_sum.S
        - boot.S
        - virt.lds
    - HW3-1
        - fibonacci.S
    - HW3-2
        - fibonacci.S
        - fibonacci.c
        - boot.S
        - virt.lds
    - HW3-3
        - main.c
        - sudoku_2x2_c.c
        - sudoku_2x2_c.h
        - sudoku_2x2_asm.S
        - boot.S
        - virt.lds
    - Bonus
        - readme.txt

## (Update 補充說明)
在 Venus的 simulator中會使用 ecall作為程式結束時需要呼叫的指令, 這個 ecall指令會根據 `a0` register所擁有的值做出相對應的動作。

在 Lab3中同學可能會遇到 Venus simulator==跳出 ecall error的訊息==, 這是因為 TA一開始的設定是希望同學單純使用 ecall指令作為 assembly的結束, 而資料以 `a0`和 `a1` register儲存, 所以沒有考慮 ecall error出現的情形。

如果同學想要修掉 ecall error這個問題的話, ==**可以將原先 `a0`, `a1` register需要儲存的值以其他 register代替, 只要用註解說明清楚就好**==。至於 ecall會因為 `a0`, `a1` register而產生甚麼樣相對應的動作, 同學想要了解的話可以參考下面這個 link, 會對 ecall有更深入的了解 :
- https://github.com/kvakil/venus/wiki/Environmental-Calls

## RISC-V Assembly Programming
### References

如果你對於如何寫RISC-V Assembly 的方式不夠瞭解, 你可以看看下面的youtube video 加強你對RISC-V Assembly programming 的背景知識, 如果你要寫比較複雜的應用程式, 你會需要了解Assembly program 的function/procedure calls 是如何被實作的, 每一個ISA 都會定義自己的calling convention, 你可以參考附上的reference, 加強自己對RISC-V calling convention 的理解

- youtube video by Gedare Bloom @ University of Colorado, Colorado Springs
    - [Assembly Programming with RISC-V: Part 1 - Assembly Basics, Registers, and Load/Store Instructions](https://www.youtube.com/watch?v=XVU_RNiz09A)
    - [Assembly Programming with RISC-V: Part 2 - Arithmetic/Logic Instructions](https://www.youtube.com/watch?v=PMzbRMsGpwU)
    - [Assembly Programming with RISC-V: Part 3 - Control Flow Instructions](https://www.youtube.com/watch?v=QfXav1X7EP8)
    - [Assembly Programming with RISC-V: Part 4 - Function Calls (Procedure)](https://www.youtube.com/watch?v=GFlkl2DKqiY)
- [RISC-V Assembly Programmer's Manual](https://github.com/riscv-non-isa/riscv-asm-manual/blob/master/riscv-asm.md)
- [Understanding RISC-V Calling Convention](https://inst.eecs.berkeley.edu/~cs61c/resources/RISCV_Calling_Convention.pdf)
- [RISC-V Instruction Set Manual, Volume I: RISC-V User-Level ISA](https://five-embeddev.com/riscv-isa-manual/latest/preface.html#preface)
>[name=陳慕丞][RISC-V Instruction Set Specifications](https://msyksphinz-self.github.io/riscv-isadoc/html/index.html)

### Venus Simulator
-  Lab3-1, Lab3-2的作業可以透過線上網站 Venus 進行編譯與除錯，請先進入 Venus網站。
    -  Venus website link : https://www.kvakil.me/venus/
- Venus 基本操作說明
    1. 網站中分為 `Editor` 和 `Simulator` 兩種模式。請在 `Editor` 中放入想要模擬的程式碼，完成後可以在 `Simulator` 中查看模擬結果。
    2. `Simulator` 模式動作說明。
:::warning
   - 系統預設 `ecall` 為程式碼**終止指令**。
   - 程式中請使用 `#` 替程式寫上註解。
   - 程式中 function 所使用的標籤 (label) 請以 `:` 作結尾。
   - 程式中一行僅能接受一個指令。
   - 所編寫之程式碼會被**逐行讀取**，編寫時請依照**執行順序依序撰寫**。
   - 完成程式碼編寫後請切換到 `Simulator` 模式查看程式執行結果，若是有不可接受的語法系統會跳出提醒，依據提醒修改即可。
   - example : 
```mipsasm=
main:
#your main function 

label:
#your call function

end:
ecall
```
:::


:::danger
- 注意 : 網頁並<font color='#f00'>**不會儲存**</font>你所撰寫的程式碼，在**重新整理**之前請先保存程式碼。
:::
- 對於一般的新手而言, 因為Venus simulator 可以讓你觀察每一條指令執行之後Register/Memory 值的變化, 你會比較容易知道自己的程式碼哪裡出現問題, 如果沒有這樣的simulator, Assembly programming 的偵錯通常會比較困難, 有經驗的Assembly programmer, 常常會有自己的偵錯方法, 比如寫一個能用C printf() debug 的框架, 我們會在 Lab3-3給大家一個範例參考。

### Lab3-1
- 在熟悉 Venus 操作後，下方提供 `lab3-1_ex_c.c` 和 `lab3-1_ex_assembly.S` 程式碼，這兩份程式碼為==相互轉換後的結果==。**請將 `lab3-1_ex_assembly.S`透過 Venus 進行模擬功能**並回答下列問題。這些問題的答案==不需要交回==，但是許多重要概念在 HW3中會被大量使用，希望同學熟悉。
- `lab3-1_ex_assembly.S`會將 `source[]`中的資料 copy到 `dest[10]`中，同學在進行模擬時可以在 Simulator模式中將右方的 Register欄位切換至 Memory觀察資料移動和指令的關係。
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_8af5dd4f86e4cfe3dc42d89428bbc044.png)
- 問題討論 : 
    1. 在 C file 中參數 `int k` 相對應的暫存器為何。
    2. `source` 和 `dest` 的資料在 assembly code中如何利用。
    3. 在組合語言 `lab3_ex_assembly.s` 中是如何進行 for迴圈。
    4. 在組合語言 `lab3_ex_assembly.s` 中是如何更改指標 (pointer) 中的值，儲存 & 拿取所需要的資料。
    5. 在組合語言 `lab3_ex_assembly.s` 中如何預先在記憶體中給定資料，資料型態、資料大小有甚麼需要限制的。
- `lab3-1_ex_c.c`
```cpp=
//lab3_ex_c.c
int source[] = {3, 1, 4, 1, 5, 9, 0};
int dest[10];

int main () {
    int k;
    for (k=0 ; source[k]!=0 ; k++) {
	    dest[k] = source[k];
    }
    return 0;
}
```
- `lab3-1_ex_assembly.S`
```assembly=
.data
source:                    ## 對應到 int source[]的資料格式
    .word	3
    .word	1
    .word	4
    .word	1
    .word	5
    .word	9
    .word	0
dest:                      ## 對應到 int dest[]的資料格式
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0
    .word	0

.text
main:
    addi     t0, x0, 0     ## t0為和 base address的 offset
    la       t1, source    ## t1為 source資料的 base address
    la       t2, dest      ## t2為 dest資料的 base address
loop:
    slli     t3, t0, 2     ## 將 t0 shift因為一個 int資料大小為 4個 bytes，左移 2等同於乘以 4的動作 
    add      t4, t1, t3    ## 將 base address + offset
    lw       t5, 0(t4)     ## 取出記憶體中資料
    beq      t5, x0, exit  ## 取到 array結束 (結尾"0")
    add      t6, t2, t3
    sw       t5, 0(t6)     ## 存回 dest目標記憶體中
    addi     t0, t0, 1     ## 還有資料需要處理，將 t0+1後繼續執行
    jal      x0, loop  
exit:
    addi     a0, x0, 10
    add      a1, x0, x0
    ecall    #Terminate
```
### Lab3-2
#### Lab3-2-1 Calling Convention
Lab3-2-1為 calling convention的概念解釋，並**不需要利用 Venus進行模擬**。Venus模擬部分會在 Lab3-2-2中進行。
- 在 `Lab3-1`中對於 Venus 的比較熟悉後， `Lab3-2`則是要練習如何在組合語言中利用 calling convention完成遞迴 (recursive)函數。在遇到比較複雜的問題時，我們會需要利用 calling convention的方法。而遞迴則是一個不斷進行 calling convention的過程。
- 以下方的程式碼為例子：
```cpp=
//this example code is incomplete
function Recursive(int x) {
    int y = 0;
        
    //...
        
    if(x!= 0) {
        Recursive(y);            //"self-calling"，開始進行遞迴動作
    }
        
    //...
        
    int z = Recursive(y) + 1;    //遞迴完成後，從這裡接續下去執行
}
```
- 觀察上方的範例程式中的第7行，當程式需要進行 calling convention動作時，需要被儲存的值為：
    1. 函式的參數值 (e.g. x值)。
    2. 函式內暫存變數的值 (e.g. y值)。
    3. 返回位址 (return address)。遞迴完成後，接續執行的程式位置。
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_7646564d47a7c6b5b6365293a764fd37.png)
- 而在 assembly code中，我們會利用 stack pointer + memory進行資料的儲存，可以參考下方程式碼。程式碼主要進行 stack pointer移動，並儲存我們在上面說的**需要被儲存的值**，藉此確保程式需要呼叫另外一個 function或是返回上一個 function時資料正確。

```mipsasm=
call_function:
    addi   sp, sp, -8       #moving stack pointer from (sp) to (sp-8)
    sw     ra, 0(sp)        #store "return address" into memory
    sw     t0, 4(sp)        #store "temp data" into memory
        
return:
    lw     t1, 4(sp)        #load "temp data"
    lw     ra, 0(sp)        #load "return address"
    addi   sp, sp, 8        #move stack pointer from (sp-8) to (sp)
        
    jr     ra               #jump back to return address
```

#### Lab3-2-2 Recursive Function
Lab3-2-2為上方 calling convention的練習，會完成一個階乘的 function。同學可以將 Lab3-2-2提供的 `lab3_ex2_assembly.S`**利用 Venus觀察模擬結果**。
- 下方 `Lab3-2` 利用組合語言完成階乘 (Factorial) 的運算。給定輸入 ==$n$== ，使程式可以回傳 ==$n!$== 之值。定義輸入 ==$n$== 會被放在 `x10` (register a0) 中，而輸出的結果 ==$n!$== 需要被放在 `x11` (register a1)中。
    - ex : input ==$n=5$==, output ==$5!=120$==.
    - `lab3_ex2_assembly.S`相對的 memory位置也一併放在下方，同學可以互相參照。

- `lab3_ex2_assembly.S`
```mipsasm=
## 為了方便同學理解遞迴的 calling convention如何進行，此範例設定 n=2
## 同學可以嘗試更大的數字並利用 Venus模擬觀察結果。
    
main:
    li     a0, 2              # 暫存器 a0 儲存 n 值，這邊設定 n=2
    jal    fact               # 開始進行 factorial 運算
    j      exit
fact:
    addi   sp, sp, -8         # 進行 stack pointer 的移動
    sw     ra, 0(sp)          # 儲存 return address
    sw     a0, 4(sp)          # 儲存 temp data

    li     t0, 2              
    blt    a0, t0, ret_one    # 檢查是否運算到 n=1 

    addi   a0, a0, -1         # 確認目前之n 沒有 >= 2 則繼續 call function
    jal    fact       
    lw     t0, 4(sp)          
    mul    a1, t0, a1 
    j      done

ret_one:
    li     a1, 1

done:
    lw     ra, 0(sp)
    addi   sp, sp, 8 
    jr     ra     
    
exit:
    ecall                    # Terminate

```  
- Memory 

    <img src="https://playlab.computing.ncku.edu.tw:3001/uploads/upload_b6a8e0d09226064901f5ec866eee1137.png
" width="80%" height="80%" align=center>

可以看到在 Venus中記憶體顯示的儲存情形如上圖。因為 `jal`指令會將 pc+4的值儲存至 `ra`暫存器中，所以可以觀察到 `0x7fffffe8`記憶體中的值為 8。
```armasm=6
jal    fact       ## PC = 4
j      exit       ## PC = 8
```
繼續觀察第二次的 `jal`發現 `0x7fffffe0`記憶體中的值為 40，所以 `jal`可以在 function return後回到正確的 pc位址繼續運作。
```armasm=17
jal    fact       ## PC = 36
lw     t0, 4(sp)  ## PC = 40   
```
:::warning 
- Calling convention的動作在 HW3中會佔很大一部分的比例，請確實熟悉 `Lab 3-1` 和 `Lab 3-2` 的內容。
:::

### Lab3-3 

在Lab 3-1 跟Lab 3-2 中, 我們寫的Assembly file 在Venus simlator 上執行, 與一般RISC-V machine 執行程式碼的實際流程是不太一樣的, 為了教學目的, Lab 3-1 & 3-2 簡化了整個流程, 並且使用文字格式檔案作為simulator 的input, 但是這不是真實硬體會執行的檔案格式, 在Lab 3-3, 我們提供一個在RISC-V machine 上程式碼能被執行的一個正常流程讓同學參考, 希望同學能了解下面幾件事情

- 在真正的硬體上, 要執行一個程式碼時, 如何能編譯產生硬體能接受的檔案格式
- 如果我們希望開發過程中所寫的程式碼, 不需要經過修改, 就能在開發出來的硬體上面執行時, 那我們的開發流程會長什麼樣子, 同時模擬硬體行為的軟體模擬器會長什麼樣子

#### Working Environment Setup 

:::info
Prepare RISC-V Toolchain & QEMU Emulator
:::
在 Lab3-3及本次 HW中我們所使用的 cpu emulator會以 QEMU為主， 檔案部分已經幫同學準備好放在 docker中，但是會需要同學**在進入 docker後依照下方指令 build up QEMU**，在 Lab3-3及後面的作業才能順利地進行模擬。
- QEMU Pros and Cons
    - Pros 
        - 執行速度很快可以在上面跑OS 或很複雜的程式
        - 如果用來做開發晶片流程中的硬體模擬器, 上面的軟體可以幾乎不用修改, 在將來直接跑在設計出來的晶片上
    - Cons
        - 比較複雜, 要修改要增加新的指令的話, 必須對QEMU 本身的設計原理有深入的了解，相對於將來Lab4 我們要實作的emulator 困難度多很多 
- build up QEMU
```shell=
$ cd /workspace/projects/qemu
$ ./configure --target-list=riscv32-linux-user,riscv32-softmmu

## It may take some times...
$ make -j $(nproc)
$ sudo make install

## check search path can find qemu
$ which qemu-system-riscv32
/usr/local/bin/qemu-system-riscv32 
```

> [name=施宇庭]
> 重開 container 後可以不用 rebuild QEMU 的指令
> ```shell
> ## 編譯及安裝 QEMU (重開 container 不用重做的部分)
> $ QEMU_PATH='/workspace/projects/qemu-riscv32'
> $ cd /workspace/projects/qemu
> $ ./configure --target-list=riscv32-linux-user,riscv32-softmmu --prefix="$QEMU_PATH"
> $ make -j $(nproc)
> $ make install   # (可以不用 sudo)
> 
> ## 將 QEMU 的安裝路徑新增到環境變數 (重開 container 需要重做的部分)
> $ QEMU_PATH='/workspace/projects/qemu-riscv32'
> $ echo 'export PATH="$PATH:$QEMU_PATH/bin"' >> ~/.bashrc
> $ source ~/.bashrc
>
> ## check search path can find qemu
> $ which qemu-system-riscv32
> /workspace/projects/qemu-riscv32/bin/qemu-system-riscv32
> ```
> 

<!--
- [rv32emu - RISC-V RV32I emulator with ELF support](https://github.com/sysprog21/rv32emu) - Pros and Cons
    - lightweight (**+**)
    - Easy to use - A simple emulator relys on the SDL library to  provide low level access to audio, keyboard, mouse, joystick, and stdlib functions such as printf() (**+**)
    - Can only verify user-mode program with no support for OS and system calls (**-**)
-->
<!--        
    - Installation

    ```shell=
    ## install dependency
    $ sudo apt install libsdl2-dev
    ## apt-get update
    ## Fetch rv32emu and build from source
    $ git clone https://github.com/sysprog21/rv32emu
    
    $ cd rv32emu

    ## build emulator
    $ make

    ## Validate 
    $ make check

    ## Run RV32I ISA tests
    $ make arch-test RISCV_DEVICE=I
    ```
-->


<!--- add QEMU support into Dockerfile 

    ```Dockerfile=
    ## add the following dependency
    ENV QEMU_PACKAGES build-essential \
            ccache \
            chrpath \
            clang \
            cpio \
            diffstat \
            gawk \
            gettext \
            git \
            git-core \
            glusterfs-common \
            libaio-dev \
            libattr1-dev \
            libbrlapi-dev \
            libbz2-dev \
            libcacard-dev \
            libcap-ng-dev \
            libcurl4-gnutls-dev \
            libdrm-dev \
            libepoxy-dev \
            libfdt-dev \
            libgbm-dev \
            libibverbs-dev \
            libiscsi-dev \
            libjemalloc-dev \
            libjpeg-turbo8-dev \
            liblzo2-dev \
            libncursesw5-dev \
            libnfs-dev \
            libnss3-dev \
            libnuma-dev \
            libpixman-1-dev \
            librados-dev \
            librbd-dev \
            librdmacm-dev \
            libsasl2-dev \
            libseccomp-dev \
            libsnappy-dev \
            libspice-protocol-dev \
            libspice-server-dev \
            libssh-dev \
            libssl-dev \
            libusb-1.0-0-dev \
            libusbredirhost-dev \
            libvdeplug-dev \
            libvte-2.91-dev \
            libzstd-dev \
            locales \
            make \
            ninja-build \
            python3-yaml \
            python3-sphinx \
            python3-sphinx-rtd-theme \
            samba \
            sparse \
            texinfo \
            xfslibs-dev

    RUN DEBIAN_FRONTEND=noninteractive apt-get -y install $QEMU_PACKAGES
    ```
    - compile and install qemu
    ```shell
    ## clone qemu in run.sh
    $ cd /workspace/projects 
    $ git clone https://git.qemu.org/git/qemu.git

    ## compile the simulator after the Docker Container is brought up. (e.g. startup.sh)
    ## configure qemu
    $ cd qemu
    $ ./configure --target-list=riscv32-linux-user,riscv32-softmmu 
    ## apt get ninja-build
    ## apt-get install libmount-dev libpixman-1-dev
    ## 加進去dockerfile

    ## compile qemu
    $ make

    ## install qemu
    $ make install
    ```-->
而使用 QEMU需要用到的 cross compiler & toolchain也一併幫同學準備並且設置好了。接下來請**依照下方流程完成 Lab3-3**即可。而在 Lab3-3資料夾中的 `boot.S`和 `virt.lds`檔案是完成 qemu模擬所需要用到的檔案，在 Lab3中並不會解釋用途，同學如果想了解可以參考下面的網址 : 
https://playlab.computing.ncku.edu.tw:3001/Uz0ysV4xRteEemkKBlSHEg


 
    
<!--
```shell
## clone toolchain
$ mkdir -p /workspace/projects/riscv-tools
$ cd /workspace/projects/riscv-tools
$ wget https://buildbot.embecosm.com/job/riscv32-gcc-ubuntu1804-release/10/artifact/riscv32-embecosm-ubuntu1804-gcc11.2.0.tar.gz
$ tar zxvf riscv32-embecosm-ubuntu1804-gcc11.2.0.tar.gz

## Add the toolchain to the search path
export PATH=/workspace/projects/riscv32-tools/riscv32-embecosm-ubuntu1804-gcc11.2.0/bin:$PATH
```
-->
<!--
- Option 2 - download & install [The xPack GNU RISC-V Embedded GCC](https://xpack.github.io/riscv-none-embed-gcc/)
    - The gcc version is older. However, it's used by the RV32EMU project.
--> 

<!--
```shell=
$ mkdir -p /workspace/projects/riscv-tools
$ cd /workspace/projects/riscv-tools
$ wget https://github.com/xpack-dev-tools/riscv-none-embed-gcc-xpack/releases/download/v10.2.0-1.1/xpack-riscv-none-embed-gcc-10.2.0-1.1-linux-x64.tar.gz
$ tar zxvf xpack-riscv-none-embed-gcc-10.2.0-1.1-linux-x64.tar.gz

## Add the toolchain to the search path
export PATH=/workspace/projects/riscv32-tools/xpack-riscv-none-embed-gcc-10.2.0-1.1/bin:$PATH
```
-->
#### Compile a RISC-V binary in the ELF file format
根據下方指令操作我們可以先將原先包含在 Lab3-3資料夾中的 `hello_world.c` compile成 RISCV binary的形式。完成後先將assembly dump出來，再利用指令查看一下所產生的 ELF file資訊。
- Command
```shell
## open Lab3-3 folder
$ cd /workspace/projects/lab3/Lab3-3

## checkout all files
$ ls -l
-rw-r--r-- 1 base base 384 Feb 11 13:23 example_1_main.c
-rw-r--r-- 1 base base 532 Feb 11 13:23 example_1_sum.S
-rw-r--r-- 1 base base 358 Feb 11 13:23 example_2_main.c
-rw-r--r-- 1 base base 531 Feb 11 13:23 example_2_sum.S
-rw-r--r-- 1 base base 126 Feb 11 13:23 hello_world.c

## compile hello_world.c into RISC-V binary
$ riscv32-unknown-elf-gcc -nostartfiles -Tvirt.lds boot.S hello_world.c -o hello_world

## dump assembly
$ riscv32-unknown-elf-objdump -d hello_world

## check ELF file info
$ riscv32-unknown-elf-readelf -h hello_world
```
- `hello_world.c`
```cpp=
// Lab3-3 - hello_world.c
#include <stdio.h>
#include <stdlib.h>

int main(){
   
   printf("hello world!\n");
   return 0;
}
```
<!-- :::warning

```shell
## compile RISC-V binary
$ riscv32-unknown-elf-gcc hello.c -o hello -march=rv32i

## dump assembly
$ riscv32-unknown-elf-objdump -d hello

## check ELF file info
$ riscv32-unknown-elf-readelf -h hello
```
<!-- :::warning
- 如果有跳出 `command not found`等提醒表示所 export的路徑是錯誤的，系統無法搜尋到指令！
::: -->

:::info 
 Run Simulation
:::
#### Run a simple RISC-V ELF file on the QEMU simulator
完成上方步驟後接下來我們會使用剛剛產生的 ELF file利用 QEMU進行模擬，同學請根據下方指令操作。

```shell=
## command
$ cd /workspace/projects/lab3/Lab3-3
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel hello_world

## result
hello world!Baremetal code runs to the end. (pressing `ctrl+a x` to quit)

## 按下 `ctrl+a`, 放開後按下 `x`離開模擬。
```
:::info 
 C/Assembly Hybrid Programming
:::
#### Compile a C program with a function call implemented in assembly
剛剛我們先將 c code compile成 RISCV binary, 再利用 QEMU進行 simulation。接下來則是要示範如果我們只想要將 c code的一部分或是一個 function以 assembly code的方式去跑，應該要如何操作。

#### Example 1
  - c code在執行時如果所有 function都交給 compiler自動 compile的話，所產生的 assembly code有些是沒有效率的。在這種情況下會選擇自行手刻 assembly code並讓 c code呼叫。 Example 1中提供簡單程式舉例如何將 c code中的一個 function 轉換為 assembly code並執行。
  - sum_ex1_asm.S 的細節請參考[Understanding RISC-V Calling Convention](https://inst.eecs.berkeley.edu/~cs61c/resources/RISCV_Calling_Convention.pdf)裡面的說明，下方指令會編譯RISCV 的binary, 然後在qemu 上跑simulation, 比對C 與Assembly 的實作結果是否一致。

首先我們先利用 `example_1_main.c`和 `example_1_sum_wrong.S`進行模擬。
```shell=
## open Lab3-3 folder
$ cd /workspace/projects/lab3/Lab3-3

## compile RISC-V binary
$ riscv32-unknown-elf-gcc -nostartfiles -Tvirt.lds boot.S example_1_main.c example_1_sum_wrong.S -o example_1

## Run simulation
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel example_1

## you will see the following output
C code sum_c(5)=15

## ...your simulation will stuck, press `ctrl+a x` to exit simulation.
```  
同學應該會發現程式會卡在 `C code sum_c=15`這個部分而無法完成模擬。這是因為在 riscv中有規定 calling covention格式，如果在 call function中使用到 `s0`, `s1`, `s2`等暫存器，calling convention規定必須要在 ret時回覆該暫存器原來的值 (如下圖所示)，否則會造成程式錯誤。

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_433ede9c18b4c6e0f33e23c9953707ab.png)

而為了避免這個問題我們會使用 Lab3-2所說的 stack pointer來儲存這些暫存值，讓我們在 function中可以隨意更動 `s0`, `s1`, `s2`, 只要確保 ret時有回歸原先的值即可。接下來請同學將`example_1_main.c`中的第29行改為 :
```cpp=29
out = sum_ex1_asm(n);  ## call the function with right calling convention
```
使程式呼叫 `example_1_sum.S`這份 assembly code, 同學可以看到在這份 assembly code中我們會先使用 stack pointer & memory儲存 `s0`, `s1`, `s2`這些**在此 function中會被改動的 register**，在 ret之前再將值返還。接下來可以使用下面指令再次進行模擬，會發現這次程式就可以成功執行完畢。
```shell=
$ riscv32-unknown-elf-gcc -nostartfiles -Tvirt.lds boot.S example_1_main.c example_1_sum.S -o example_1

## Run simulation
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel example_1

## Result, code runs to the end successfully.
C code sum_c=15
ASM code sum_ex1_asm=15
Baremetal code runs to the end. (pressing `ctrl+a x` to quit)
```
:::warning
下方和lab3資料夾中均提供 lab code，請同學實際操作並了解其中的差異。
:::


- `example_1_main.c` 
```cpp=
// Lab3-3 - example_1_main.c

#include <stdio.h>
#include <stdlib.h>

int sum_c(int n){
  int temp = 0;
  for (int i = 1; i <= n; i++) {
    temp = temp + i;
  }
  return temp;
}

int puts(const char *msg);

int sum_ex1_asm(int n);

int sum_ex1_asm_wrong(int n);

int main(){
  int n = 5;
  int out = 0;
  out = sum_c(n);
  char str[25];
  itoa(out,str,10);
  puts("C code sum_c=");
  puts(str);
  puts("\n");  
  out = sum_ex1_asm_wrong(n);
  puts("ASM code sum_ex1_asm=");
  itoa(out,str,10);
  puts(str);
  puts("\n");
  return 0;
}
```
- `example_1_sum_wrong.S`
```armasm=
    .text                               # code section 
    .global sum_ex1_asm_wrong           # declar the sum_asm function as a  global function
    .type sum_ex1_asm_wrong, @function        # define sum_asm as a function 
sum_ex1_asm_wrong:
prologue :
    li   s0,1                  # s0 -> int i
    mv   s1,a0                 # s1 -> int n
    mv   s2,zero               # s2 -> int temp
loop_start :
    bgt  s0,s1,loop_end
    mv   a0,s0
    add  s2,s2,a0 
    addi s0,s0,1 
    j    loop_start
loop_end :
    mv   a0,s2

epilogue :
    jr   ra

    .size sum_ex1_asm_wrong, .-sum_ex1_asm_wrong
```

- `example_1_sum.S`
```armasm=
    .text                        # code section 
    .global sum_ex1_asm          # declar the sum_asm function as a  global function
    .type sum_ex1_asm, @function # define sum_asm as a function 
sum_ex1_asm:
prologue :
    addi   sp,sp, -16 
    sw     ra,0(sp) 
    sw     s0,4(sp) 
    sw     s1,8(sp)
    sw     s2,12(sp)
    li     s0,1 
    mv     s1,a0 
    mv     s2,zero
loop_start :
    bgt    s0,s1,loop_end
    mv     a0,s0 
    add    s2,s2,a0 
    addi   s0,s0,1 
    j      loop_start
loop_end :
    mv     a0,s2

epilogue :
    ## Result will be correct if you did not store "ra" 
    lw     ra,0(sp)   
    lw     s0,4(sp)
    lw     s1,8(sp)
    lw     s2,12(sp) 
    addi   sp,sp,16 
    jr     ra

    .size sum_ex1_asm, .-sum_ex1_asm
```


#### Example 2
Example 1 提供了一個簡單 assembly code範例，Example 2則是考慮在一個 function中繼續呼叫另外一個 function的動作，和 Example 1中相同會需要使用到 calling convention的動作。 Example 1 & Example 2的模擬結果會相同，但是所應用的情境有所差異。請同學一樣利用下方指令在 qemu上進行模擬。
```shell=
$ cd /workspace/projects/lab3/Lab3-3

## compile RISC-V binary
$ riscv32-unknown-elf-gcc -nostartfiles -Tvirt.lds boot.s example_2_sum.S example_2_main.c -o example_2

## Run simulation
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel example_2

## you will see the following output
C code sum_c(5)=15
ASM code sum_asm(5)=15
```  

- `example_2_main.c`
```cpp=
// main.c
#include <stdio.h>
#include <stdlib.h>

int formula(int n){
  return (1+n)*n/2; 
}

int sum_c(int n){
  return formula(n);
}

int sum_asm(int n);

int main(){
  int n = 5;
  int out = 0;
  out = sum_c(n);
  printf("C code sum_c(%d)=%d\n", n, out);  
  out = sum_asm(n);
  printf("ASM code sum_asm(%d)=%d\n", n, out);
  return 0;
}
```
同學可以注意到下方的 `example_2_sum.S`我們並沒有如同 `example_1_sum.S`一樣先將 `s0`, `s1`, `s2`這些暫存器的資料利用 stack pointer儲存，而是改用 `t0`, `t1`, `t2`代替。可以這樣做的原因是因為在 calling convention的規範中 `t0`, `t1`, `t2`暫存器並沒有被規定在 function結束後需要返還原本的值，所以這樣使用並不會造成問題。

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_677e12f403993b93e2be2c308b3819d1.png)


- `example_2_sum.S`
```armasm=
    .text                        # code section 
    .global sum_ex2_asm          # declar the sum_asm function as a  global function
    .type sum_ex2_asm, @function # define sum_asm as a function 
sum_ex2_asm:
prologue :
    addi sp,sp,-4
    sw   ra,0(sp)

funct_start :
    jal  formula

epilogue :
    lw   ra,0(sp)
    addi sp,sp,4
    jr   ra

formula :
    mv   t0,a0
    li   t1,1
    li   t2,2
    add  t1,t0,t1
    mul  t1,t0,t1
    div  t1,t1,t2
    mv   a0,t1
    jr   ra
    
    .size sum_ex2_asm, .-sum_ex2_asm
```

<!--
:::warning
- 使用原先的指令進行模擬會發現無法處理 `mul`或是 `div`等指令，這是因為 rv32i只有支援 base integer instruction set，並沒有支援 M-standard extension中的指令。可以將指令改成 rv32g才能狗順利模擬。
```shell
## original command
$ riscv32-unknown-elf-gcc  example_2_sum.S example_2_main.c -o example_2 -march=rv32i
```
```shell
## modified command
$ riscv32-unknown-elf-gcc  example_2_sum.S example_2_main.c -o example_2 -march=rv32m
```
- 根據版本的不同， RISV還可以往下細分，像是 rv32e, rv32m等等，其中 rv32g為包含所有 extension instruction set，想更深入了解可以參考 [RISC-V Instruction Set Manual, Volume I: RISC-V User-Level ISA](https://five-embeddev.com/riscv-isa-manual/latest/preface.html#preface).
-->

#### Run a sample baremetal code on QEMU (Optional)

在晶片開發的流程中, 為了減少晶片製造出來之後的移植(porting)工作, 也為了能讓早期的軟體開發更貼近真實硬體製造出來的需求, 很多開發者會採用更複雜的硬體模擬軟體, 比如說QEMU 來作為軟體開發的平台, 也因此在上面開發出來的binary 就必須要處理一些與硬體相關的參數設定與設計假設, 下面的文件中描述了一個完整的baremetal framework是如何實作出來的, 在這份文件中所使用的QEMU, 是一個RISC-V full-system emulator, 可以在上面跑完整的OS (e.g. Linux) 以及跟晶片量產之後幾乎相同的software stack. 

- [Playlab Baremetal Framework on RISCV](https://playlab.computing.ncku.edu.tw:3001/Uz0ysV4xRteEemkKBlSHEg?view)

## Debug Tips
### Entry point address & setting the CPU’s program counter
如果在實作的過程中遇到問題，提供同學一些可以找到 bug的方法。先利用 [Entry point address & setting the CPU’s program counter](https://playlab.computing.ncku.edu.tw:3001/Uz0ysV4xRteEemkKBlSHEg?view#Entry-point-address-amp-setting-the-CPU%E2%80%99s-program-counter)找到利用qemu進行模擬實的entry point，使用以下指令。
```shell=
$ readelf -h <your ELF file>
```
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_5bf41ac5ea65c511971172ab6a7af2e4.png)

以 `example_1`的 ELF file為例，TA的 entry point為 0x80000000。

### Enabling QEMU instruction tracing
接著再利用 [Enabling QEMU instruction tracing](https://playlab.computing.ncku.edu.tw:3001/Uz0ysV4xRteEemkKBlSHEg?view#Enabling-QEMU-instruction-tracing)查看 assembly code的執行情形，指令如下。
```shell=
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel <your Elf file> -d in_asm
```
同樣以 `example_1`的 ELF file為例，會發現如果使用 `example_1_asm_wrong .S`這個錯誤的 assembly code進行模擬會停在 `x800000e0`這個 address。

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_4756bc2d1938d327e74696197a2ecd69.png)

### Object Dump
最後利用下方指令將 assembly code dump出來，再回去對照上方 assembly code卡住的位址。

```shell=
$ riscv32-unknown-elf-objdump -d <your ELF file>
```

依樣以 `example_1`為例。使用此指令會出現如下圖包含 function, address & instruction的資訊，這時候我們需要去找到剛剛程式停止的那個 address (上方範例為 `x800000e0`這個address)，根據它去做 debug。

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_b9c0c6c32b5712e707efadcc0c34c905.png)


依上圖為例就是在 `example_1_main.c`中的
```cpp=29
out = sum_ex1_asm_wrong(n);
```
完成後要進行下一行
```cpp=30
puts("ASM code sum_ex1_asm=");
```
發生了錯誤，同學在遇到錯誤時可以參考這個流程進行 debug。

### Debug Tips Reference
- https://playlab.computing.ncku.edu.tw:3001/Uz0ysV4xRteEemkKBlSHEg?view#Enabling-QEMU-instruction-tracing


## Homework
### HW Preparation and Background
- HW3-1 需要利用組合語言完成費波那契數列 (Fibonacci Series) 的運算，詳細作業要求在 HW3-1 中。作業中需要利用組合語言進行**遞迴**的動作，仍未熟悉組合語言遞迴部分可以參考 `exercise2` 或是老師上課內容。
- HW3-2 會利用 Lab3-3所使用的 qemu emulator進行操作，使用 c code和 assemnly code交叉驗證同學所做的 HW3-1的正確性。
- HW3-3 作業需要利用組合語言完成一數獨演算法，此 LAB3 參考 [UCI CS152 LAB1](https://www.ics.uci.edu/~swjun/courses/2021W-CS152/material/lab1.pdf) 之組合語言練習。作業中同樣會需要運用到**遞迴**的手法。
- 作業中會需要用到的指令如下，請同學請自行將指令更換成所需要模擬的檔案
```shell=
$ cd /workspace/projects/lab3/<your_folder_name>

## compile RISC-V binary
$ riscv32-unknown-elf-gcc -nostartfiles -Tvirt.lds boot.S <assembly_code> <c_code> -o <object_name>

## Run simulation
$ qemu-system-riscv32 -nographic -machine virt -bios none -kernel <object_name>
```

### HW3-1 費波那契數列 Fibonacci Series
- HW3-1 請完成費波那契數列演算法，下方提供演算法 C code 和示意圖。
- 作業要求：
    1. 請完成 HW3-1並在 **Venus上完成模擬並依照 homework template繳交**。建議使用 Lab3-2提到的遞迴方法。
    2. 預先設定 ==$n$== 值定義在 `x10` (register a0) 中，而輸出的結果 ==$fibonacci(n)$== 需要被放在 `x11` (register a1)中。 **繳交作業時請將 n定義為 16並算出正確答案**。

- `Fibonacci_Series.c`
```cpp=
int fibonacci(int n) { 
    if(n == 0) {
        return 0;
    }

    else if(n == 1) {
        return 1;
    }

    else {
        return fibonacci(n-1)+fibonacci(n-2);        
    }
}
```
- 演算法示意圖：
<center><img src="https://playlab.computing.ncku.edu.tw:3001/uploads/upload_89df008828e45921944ccee06eda0110.png
" alt="Websites-interlinking-to-illustrate-Page-Rank-percents" style="width: 400px"></center>

### HW3-2 C/Assembly Hybrid Programming 
<!--[TODO] 讓同學練習用Lab 3-3 的hybrid framework 進行Sudoku C 與Assembly implementation 的比對-->
- 完成 HW3-1後請依照 Lab3-3的方法產生出 ELF file並完成 HW3-1中費波那契數列 function的模擬，作業要求和程式碼格式可以參照下方作業範例 `main.c`和 `fibonacci.S`。
- 作業要求
    - 請依照下方範例程式碼完成作業。
    - 完成後請依照 **HW Preparation and Background**的指令進行檔案的 compile & simulation.
- `main.c`
```cpp=
//main.cpp
#include <stdio.h>
#include <stdlib.h>


int fibonnaci(int n){

//TODO

}

int fibonnaci_asm(int n);

int main(){
    int n = 6     //setup input value n, modify by yourself
    int out = 0;
    out = fibonacci_c(n);
    printf("C code fibonnaci_c(%d)=%d\n", n, out);  
    out = fibonnaci_asm(n);
    printf("ASM code fibonnaci_asm(%d)=%d\n", n, out);
    return 0;
} 
```
- `fibonacci.S`
    
```armasm=
    .text                          # code section 
    .global fibonacci_asm          # declar the sum_asm function as a  global function
    .type fibonacci_asm, @function # define sum_asm as a function 
fibonnaci_asm:

    #TODO

    .size fibonnaci_asm, .-fibonnaci_asm
```


### HW3-3 2x2 Sudoku
HW3-3需要完成2x2簡易數獨的組合語言，數獨規則說明和作業要求如下。
<!--> [name=weifen] 這裡不要放link, 你應該自己寫一個說明, 因為上面link 的內容不是都適用於我們, 比如我們用的simulator 跟工作環境不太一樣 [name=weifen] 這部分你有想過你要怎麼改作業嗎？-->
- 作業要求 & 注意事項：
    1. 實作C 版的演算法在 main.c:sudoku_2x2_c()並更改 test_c_data中的值。
    2. 實作Assembly 版的演算法在 sudoku_2x2_asm.S並更改 test_asm_data中的值。
    3. 撰寫數獨演算法之測資可以參考main.c 裡所提供的 test_c_data 跟test_asm_data，你也可以修改這兩個array以自己的資料進行測試。你繳交作業後, TA會以 TA的測資 (共三筆) 進行驗證, 但是TA 的測資不會事先公告給大家。
    4. 請依照lab3/HW3-3 所提供的code template 完成此次作業並依照 Lab3-2, Lab3-3的教學利用 qemu完成模擬。
    5. 完成模擬出現的 *same result* 或是 *different result*等字樣只是用來判斷你用 c code以及 assembly code所完成的 function是一樣的，**==並不代表所解出來的數獨答案是對的==**，請同學特別注意。
- 數獨規則：
    <img src="https://playlab.computing.ncku.edu.tw:3001/uploads/upload_51e0aa2d566ab26e169aac56f50911e5.png
    " alt="Websites-interlinking-to-illustrate-Page-Rank-percents" style="width: 250px">   <img src="https://playlab.computing.ncku.edu.tw:3001/uploads/upload_193f1a4c91869be802b43adea7c156a4.png
" alt="Websites-interlinking-to-illustrate-Page-Rank-percents" style="width: 248px">

    1. 每一行、列和子方框中不可出現重複數字。
    2. 以 2x2 大小的數獨為例，可出現的數字為1~4。
    3. 提供之測試資料中，`0` 為空白，即需要被填入的資料。

- 演算法：
    - 數獨演算法下方 TA有提供 `Backtracking Algorithm`的 pseudocode，撰寫作業時可以直接使用此演算法。
       
    - 除了 `Backtracking Algorithm` 演算法外，也可以自行上網搜尋演算法，確保答案正確即可。

    - `Backtracking Algorithm`
```cpp=
// backtracking algorithm
boolean solve(int index) {
    if ( index >= 16 ) {
        return true;                                 // 如果檢查完所有的格子，回傳 True
    }                    
    if ( set[index] > 0 ) {                          // set是一個儲存所有資料的array
        return solve(index+1);                       // 如果格子中已經有值了則會往下一格判斷

    }

    else {
        for ( n = 1 to 4 ) {                         // 判斷目前這格在 1~4是否有符合條件
            set[index] = n;                          // 如果有的話就往下一格作判斷（遞迴）
                                                     // 直到每一格都符合條件為止

            if ( check(index) and solve(index+1) )  // check function用來檢查當前這格放入這個數值是否正確
                return true;                         // solve(index+1) function則是繼續判斷下一格的值     
        }
    }
    set[index] = 0;                                  // returns the value to 0 to mark it as empty
    return false;                                    // no solution
}
```
- HW3-3作業範本
    - `main.c`
    

```cpp=
#include <stdlib.h>
#include "sudoku_2x2_c.h"
#define SIZE 16

# You can modify your own test data here.
char test_c_data[16] = { 0, 0, 2, 0, 
                         0, 0, 0, 4,
                         2, 3, 0, 0, 
                         0, 4, 0, 0 };

char test_asm_data[16] = { 0, 0, 2, 0, 
                           0, 0, 0, 4,
                           2, 3, 0, 0, 
                           0, 4, 0, 0 };

void print_sudoku_result() {
    int i;
    char str[25];
    puts("Output c & assembly function result\n");
    puts("c result :\n");
    for( i=0 ; i<SIZE ; i++) {   
        int j= *(test_c_data+i);
        itoa(j, str, 10);
        puts(str);
    }

    puts("\n\nassembly result :\n");
    for( i=0 ; i<SIZE ; i++) {
        int j= *(test_asm_data+i);
        itoa(j, str, 10);
        puts(str);
    }

    int flag = 0;
    for( i=0 ; i<SIZE ; i++) {
        if (*(test_c_data+i) != *(test_asm_data+i)) {
            flag = 1;
            break;
        }
    }

    if (flag == 1){
        puts("\n\nyour c & assembly got different result ... QQ ...\n");
    }
    else {
        puts("\n\nyour c & assembly got same result!\n");
    }
}


void sudoku_2x2_asm(char *test_asm_data); // TODO, sudoku_2x2_asm.S

void sudoku_2x2_c(char *test_c_data); // TODO, sudoku_2x2_c.c

int main() {
    sudoku_2x2_c(test_c_data);
    sudoku_2x2_asm(test_asm_data);
    print_sudoku_result();
    return 0;
}
```
- `sudoku_2x2_asm.S`
```armasm=
    .text                           # code section 
    .global sudoku_2x2_asm          # declare the asm function as a global function
    .type sudoku_2x2_asm, @function # define sum_asm as a function 
sudoku_2x2_asm:

    #TODO

    .size sudoku_2x2_asm, .-sudoku_2x2_asm
```




## Bonus

如果你已經修過Computer Architecture 的基本課程, 你應該知道什麼是5-staged pipelined CPU, 你可以嘗試完成黃敬群老師課程裡的[Assignment1: RISC-V Assembly and Instruction Pipeline](https://hackmd.io/@sysprog/2021-arch-homework1), 請選擇與黃老師之前同學不一樣的leetcode 題目來完成該Assignment。

<!--
## Homework Document Template
- **Step 1**
    請在自己的 GitLab內建立 `lab3` repo，並將本次 Lab 撰寫的程式碼放入這個repo。 在Working environment setup 時, 有如何把你的作業程式碼推回你自己 course Gitlab 帳號的說明
    
- **Step 2**
    請參考以下連結自行建立並撰寫一份 CodiMD 作業說明文件。
https://playlab.computing.ncku.edu.tw:3001/uRwZ4ZinRQOLRILGQUQc-w
    在 [Playlab 作業中心](https://playlab.computing.ncku.edu.tw:8000) 繳交完成的 CodiMD 作業說明文件。
:::info
Deadline:11:59:59pm 2022/3/10,  Bonus can be done before the end of the semester. 
:::
-->
