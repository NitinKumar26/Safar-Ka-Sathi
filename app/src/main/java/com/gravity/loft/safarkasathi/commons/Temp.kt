package com.gravity.loft.safarkasathi.commons

class Temp {

    fun isPalindrome(input: String, withTemp: Boolean):Boolean{
        if(withTemp){
            var forward = 0
            var backward = input.length - 1

            while (backward > forward) {
                if (input[forward++] != input[backward--])
                    return false
            }
            return true
        }else{
            var temp = ""
            for(i in input.length - 1 downTo 0)
                temp+=input[i]
            return temp.equals(input)
        }
    }


//
//    private fun sendOtp(mobile: String) = launchWithProgress{
//        val user = User("en", mobile)
//
//        val response = ServiceBuilder.buildService(LoginService::class.java).registerMobile(user)
//
//        if(response.isSuccessful && response.body() != null){
//            val intent = Intent(this@SendOtp, OtpValidate::class.java).apply {
//                putExtra("user", user)
//            }
//            startActivity(intent)
//            finish()
//        }else{
//            toast(R.string.INTERNAL_ERROR)
//        }
//    }



    fun main() {
        val node_1 = Node(1)
        val node_2 = Node(2)
        val node_3 = Node(3)
        val node_4 = Node(4)
        val node_5 = Node(5)
        val node_6 = Node(6)

        node_1.nextNode = node_2
        node_2.nextNode = node_3
        node_3.nextNode = node_4
        node_4.nextNode = node_5
        node_5.nextNode = node_6


        val result = findKthElement(node_1, 3)
        if(result != null){
            print("--------------> ")
            print(result)
        }
    }


    data class Node(val value: Int){
        var nextNode: Node? = null
    }

    fun findKthElement(firstNode: Node, k: Int):Int?{

        var temp: Node? = firstNode;
        var count = 0

        while(temp!!.nextNode != null){
            temp = temp.nextNode
            count++
        }

        var kth = 0
        temp = firstNode

        while(temp!!.nextNode != null){
            if(kth == count-k)
                return temp.value
            temp = temp.nextNode
            kth++
        }

        return null

    }

    fun find3Numbers(arr: Array<Int>, sum: Int): Array<Int>? {
        val size = arr.size

        for (i in 0 until size - 2) {
            for (j in i + 1 until size - 1) {
                for (k in j + 1 until size) {
                    if (arr[i] + arr[j] + arr[k] == sum) {
                        return arrayOf(arr[i], arr[j], arr[k])
                    }
                }
            }
        }
        return null

//        Time Complexity: O(n3).
//        There are three nested loops traversing the array, so the time complexity is O(n^3)
//        Space Complexity: O(1).
//        As no extra space is required.



    }

    fun find3NumbersNext(arr: Array<Int>, sum: Int):Array<Int>? {
        val size = arr.size
        for (i in 0 until size - 2) {
            val set = hashSetOf<Int>()
            val nowSum = sum - arr[i]
            for (j in i + 1 until size){
                if (set.contains(nowSum - arr[j])) {
                    return arrayOf(arr[i], arr[j], nowSum-arr[j])
                }
                set.add(arr[j])
            }
        }
        return null
//        Time complexity: O(N^2).
//        There are only two nested loops traversing the array, so time complexity is O(n^2).
//        Space Complexity: O(N).
//        As no extra space is required.
    }

}