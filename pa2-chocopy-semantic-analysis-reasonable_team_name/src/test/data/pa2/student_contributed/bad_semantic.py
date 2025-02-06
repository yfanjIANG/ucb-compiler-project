flag: int = 5  
eecs : SODA = 1
return flag

cs164: int = 164
# Duplicate global variable declaration
flag: bool = True
class B(object): 
    num: int = 10
class C(B): 
    num: int = 164
class B(A):  
    value: int = 20
# Duplicate class declarations
class B(flag):  
    value: int = 20
# Redefining built-in type 'int'
class int(object):
    size: int = 30
#Class conflicting with global variable 'flag'
class flag(object):  
    val: int = 40

def foo(local_a:int) -> object:
    local_a: int = 3
    nonlocal x
    flag: str = "hello"
    # Duplicate local variable declaration within the same function
    local_a: bool = True  
    global flag
    # Nested function with duplicate declaration in the local scope
    def local_a() -> int:
        nonlocal y
        global w
        cs164 = 461
        return 0
    return local_a
# Function with duplicate parameter names
def duplicate_params(x: int, x: int) -> int:  
    y:int = "234234"
    return x

def reasonable() -> str:
    if True:
        return "hello cs164"



class D(object):
    j:int = 1
    def cs180(self:"D", x:int) -> int:
        return x
    
    def cs188() -> bool: 
        return true

    def cs189(self:int) -> bool: 
        return false
    
class E(D):
    def cs180(self:"E", x:str) -> int:
        return x
	def f00(self:E) -> bool:
		return true









