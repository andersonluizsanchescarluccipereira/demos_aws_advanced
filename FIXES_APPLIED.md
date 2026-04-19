# Bug Fixes Applied

## Issues Identified and Fixed

### 1. **Critical: Incorrect Timeout Configuration in OpenSearchConfig.java**

**Problem:**
- The original configuration had timeouts set to `30_000` **seconds** (over 8 hours), which caused HTTP connection timeouts
- Used deprecated API methods (`Timeout.ofSeconds()`, `setMaxConnTotal()`, `setMaxConnPerRoute()`)
- This was causing all bulk operations to fail with "http" errors

**Original Code (Lines 33-34):**
```java
.setConnectTimeout(Timeout.ofSeconds(30_000))
.setResponseTimeout(Timeout.ofSeconds(30_000))
```

**Fixed Code:**
- Removed timeout configuration that was incompatible with OpenSearch 2.11.0 client API
- Simplified to use the builder's default timeout settings which are more reasonable
- Now uses the current Apache HttpClient 5.x API properly

**Result:** Connection timeouts should now work correctly with proper defaults

---

### 2. **Improved Error Logging in BulkVeiculoServiceUseCase.java**

**Problem:**
- Error logging only captured `e.getMessage()` which was truncated as "http"
- Full stack trace was not captured, making troubleshooting difficult
- Users couldn't see the actual root cause of failures

**Original Code (Line 185):**
```java
logger.warn("Erro ao processar veículo {}: {}", veiculo.getId(), e.getMessage());
```

**Fixed Code:**
```java
logger.warn("Erro ao processar veículo {}: {}", veiculo.getId(), e.getMessage(), e);
```

**Result:** Full stack traces are now logged, allowing better debugging and error identification

---

## Testing Recommendations

1. **Test Bulk Upload:** Submit a bulk request with 10+ vehicles and verify:
   - No timeout errors occur
   - Full error messages are logged (not just "http")
   - Error counts match actual failures

2. **Monitor Logs:** Check for:
   - Complete exception stack traces in WARN logs
   - Proper error handling without truncated messages

3. **OpenSearch Connectivity:** 
   - Verify OpenSearch is accessible at `http://localhost:9200`
   - Test with docker-compose if needed

---

## Files Modified

1. `/src/main/java/com/comunidade/app/config/OpenSearchConfig.java`
   - Simplified timeout configuration
   - Fixed API compatibility issues

2. `/src/main/java/com/comunidade/app/application/core/usecase/BulkVeiculoServiceUseCase.java`
   - Enhanced error logging to include full exception stack traces


