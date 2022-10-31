## Testing Assembly for Emulator with Vector Extension

.data
A:                   
    .byte	0 1 2 3 4 5 6 7

B:                   
    .byte	0 1 2 3 4 5 6 7

C:                   
    .byte	0 0 0 0 0 0 0 0

.text
main:
la a0,A            
vle8_v v1,0(a0)

la a1,C
vse8_v v1,0(a1)
vle8_v v2,0(a1)

vadd_vv v3,v1,v2

li t4,4
vmul_vx v4,v1,t4

hcf 