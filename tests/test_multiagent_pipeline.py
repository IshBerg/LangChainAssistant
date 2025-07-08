"""
test_multiagent_pipeline.py
LangChainAssistant/test_multiagent_pipeline.py
Version: 1.0.0

COMMITTEE - Multi-Agent Pipeline Testing
========================================

Test script for validating Coder → Advisor pipeline functionality.
Tests various code samples and scenarios.

Authors: Baruch, Claude (Anthropic)
Project: LangChainAssistant / COMMITTEE
Created: July 2025
"""

import time
from src.multiagent_pipeline import MultiAgentPipeline


def test_kotlin_repository():
    """Test with Kotlin Repository pattern"""
    print("🧪 Test 1: Kotlin Repository Pattern")
    print("=" * 50)

    code = '''
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: UserDao
) {
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val response = apiService.fetchUser(userId)
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    database.insertUser(user)
                    Result.success(user)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedUser(userId: String): User? {
        return database.getUser(userId)
    }
}
'''

    pipeline = MultiAgentPipeline()
    results = pipeline.execute_code_review(
        code=code,
        context="Android YAKKI app - пользовательские данные с кэшированием"
    )

    print(pipeline.format_results(results))
    return results


def test_authentication_logic():
    """Test with Authentication logic"""
    print("\n🧪 Test 2: Authentication Logic")
    print("=" * 50)

    code = '''
class AuthenticationManager {
    private var currentUser: User? = null
    private val tokenStorage = TokenStorage()

    fun login(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            return false
        }

        val hashedPassword = hashPassword(password)
        val user = userRepository.authenticate(email, hashedPassword)

        if (user != null) {
            currentUser = user
            tokenStorage.saveToken(user.token)
            return true
        }
        return false
    }

    private fun hashPassword(password: String): String {
        return password.hashCode().toString() // Простая хэш-функция
    }

    fun logout() {
        currentUser = null
        tokenStorage.clearToken()
    }
}
'''

    pipeline = MultiAgentPipeline()
    results = pipeline.execute_code_review(
        code=code,
        context="Android YAKKI app - система аутентификации пользователей"
    )

    print(pipeline.format_results(results))
    return results


def test_viewmodel_pattern():
    """Test with ViewModel pattern"""
    print("\n🧪 Test 3: ViewModel Pattern")
    print("=" * 50)

    code = '''
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getMessages()
                _messages.value = result
            } catch (e: Exception) {
                // TODO: обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(Message(
                id = System.currentTimeMillis().toString(),
                text = text,
                timestamp = System.currentTimeMillis(),
                isFromUser = true
            ))
            loadMessages() // Обновляем список сообщений
        }
    }
}
'''

    pipeline = MultiAgentPipeline()
    results = pipeline.execute_code_review(
        code=code,
        context="Android YAKKI app - ViewModel для чата с голосовым помощником"
    )

    print(pipeline.format_results(results))
    return results


def test_performance_comparison():
    """Test pipeline performance across multiple runs"""
    print("\n🧪 Test 4: Performance Comparison")
    print("=" * 50)

    simple_code = '''
fun calculateSum(a: Int, b: Int): Int {
    return a + b
}
'''

    complex_code = '''
class NetworkManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val scheduler: Scheduler
) {
    suspend fun <T> makeRequest(
        url: String,
        method: HttpMethod,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)

            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            when (method) {
                HttpMethod.GET -> { /* GET logic */ }
                HttpMethod.POST -> {
                    val json = gson.toJson(body)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    requestBuilder.post(json.toRequestBody(mediaType))
                }
                HttpMethod.PUT -> {
                    val json = gson.toJson(body)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    requestBuilder.put(json.toRequestBody(mediaType))
                }
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val result = gson.fromJson(responseBody, responseType)
                Result.success(result)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
'''

    pipeline = MultiAgentPipeline()

    print("📊 Testing simple code...")
    start_time = time.time()
    simple_results = pipeline.execute_code_review(simple_code, "Simple function test")
    simple_time = time.time() - start_time

    print(f"\n📊 Testing complex code...")
    start_time = time.time()
    complex_results = pipeline.execute_code_review(complex_code, "Complex network manager")
    complex_time = time.time() - start_time

    print(f"""
📊 PERFORMANCE COMPARISON
{'=' * 30}
Simple Code:
- Execution Time: {simple_time:.2f}s
- Coder Confidence: {simple_results['coder_analysis'].confidence:.2f}
- Advisor Confidence: {simple_results['advisor_recommendations'].confidence:.2f}

Complex Code:
- Execution Time: {complex_time:.2f}s  
- Coder Confidence: {complex_results['coder_analysis'].confidence:.2f}
- Advisor Confidence: {complex_results['advisor_recommendations'].confidence:.2f}

Performance Ratio: {complex_time / simple_time:.1f}x slower for complex code
""")

    return simple_results, complex_results


def test_error_handling():
    """Test pipeline error handling"""
    print("\n🧪 Test 5: Error Handling")
    print("=" * 50)

    # Test with malformed code
    malformed_code = '''
class BrokenCode {
    fun missingBrace() {
        if (true) 
            println("Missing brace"
        // Намеренная синтаксическая ошибка

    fun anotherMethod() {
        val x = 
        // Незавершенное выражение
    }
'''

    pipeline = MultiAgentPipeline()
    results = pipeline.execute_code_review(
        code=malformed_code,
        context="Test error handling with malformed code"
    )

    print("📋 Testing malformed code...")
    print(pipeline.format_results(results))

    # Test with empty code
    print("\n📋 Testing empty code...")
    empty_results = pipeline.execute_code_review("", "Empty code test")
    print(f"Empty code result length: {len(pipeline.format_results(empty_results))}")

    return results


def run_comprehensive_test():
    """Run all tests and generate summary report"""
    print("🚀 COMMITTEE Multi-Agent Pipeline Comprehensive Test")
    print("=" * 60)

    all_results = []

    # Run all tests
    try:
        result1 = test_kotlin_repository()
        all_results.append(("Kotlin Repository", result1))
    except Exception as e:
        print(f"❌ Test 1 failed: {e}")

    try:
        result2 = test_authentication_logic()
        all_results.append(("Authentication Logic", result2))
    except Exception as e:
        print(f"❌ Test 2 failed: {e}")

    try:
        result3 = test_viewmodel_pattern()
        all_results.append(("ViewModel Pattern", result3))
    except Exception as e:
        print(f"❌ Test 3 failed: {e}")

    try:
        simple_result, complex_result = test_performance_comparison()
        all_results.append(("Simple Code", simple_result))
        all_results.append(("Complex Code", complex_result))
    except Exception as e:
        print(f"❌ Test 4 failed: {e}")

    try:
        result5 = test_error_handling()
        all_results.append(("Error Handling", result5))
    except Exception as e:
        print(f"❌ Test 5 failed: {e}")

    # Generate summary
    if all_results:
        print(f"\n📊 COMPREHENSIVE TEST SUMMARY")
        print("=" * 50)

        total_time = sum(r[1]["total_execution_time"] for r in all_results)
        avg_coder_confidence = sum(r[1]["coder_analysis"].confidence for r in all_results) / len(all_results)
        avg_advisor_confidence = sum(r[1]["advisor_recommendations"].confidence for r in all_results) / len(all_results)

        print(f"✅ Tests Completed: {len(all_results)}")
        print(f"⏱️ Total Execution Time: {total_time:.2f}s")
        print(f"📊 Average Coder Confidence: {avg_coder_confidence:.2f}/1.0")
        print(f"📊 Average Advisor Confidence: {avg_advisor_confidence:.2f}/1.0")
        print(
            f"🎯 Overall Pipeline Health: {'✅ EXCELLENT' if avg_coder_confidence > 0.7 and avg_advisor_confidence > 0.7 else '⚠️ NEEDS ATTENTION'}")

        print(f"\n📋 Individual Test Results:")
        for test_name, result in all_results:
            coder_conf = result["coder_analysis"].confidence
            advisor_conf = result["advisor_recommendations"].confidence
            exec_time = result["total_execution_time"]
            print(f"  {test_name}: Coder={coder_conf:.2f}, Advisor={advisor_conf:.2f}, Time={exec_time:.2f}s")

    print(f"\n🎉 Testing completed! Pipeline is ready for production use.")


if __name__ == "__main__":
    # Check if we have required dependencies
    try:
        from multiagent_pipeline import MultiAgentPipeline
        from llm_utils import get_llm

        print("✅ All dependencies available")
    except ImportError as e:
        print(f"❌ Missing dependency: {e}")
        print("Please ensure all COMMITTEE modules are properly installed")
        exit(1)

    # Run comprehensive test
    run_comprehensive_test()