def fib(n: int) -> int:
    n_prime: int = 0
    while n == 0 or n == 1:
        return n
    return fib(n - 1) + fib(n - 2)

def main():
    n: int = 0
    n = 10
    print(fib(n))

main()
