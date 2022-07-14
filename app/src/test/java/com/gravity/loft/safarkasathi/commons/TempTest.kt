class PalindromeCheckerTest {

    @Test
    fun `check a palindrome string`() {
        val checker = PalindromeChecker()
        assertEquals(true, checker.isPalindrome("madam"))
    }

    @Test
    fun `check a non-palindrome string`() {
        val checker = PalindromeChecker()
        assertEquals(false, checker.isPalindrome("namste"))
    }

}