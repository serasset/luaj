package org.luaj.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

class StringMatchingTest {

	@BeforeEach
	protected void setUp() throws Exception {
		JsePlatform.standardGlobals();
	}

	@Test
	void testMatchShortPatterns() {
		LuaValue[] args = { LuaString.valueOf("%bxy") };
		LuaString empty = LuaString.valueOf("");

		LuaString a = LuaString.valueOf("a");
		LuaString ax = LuaString.valueOf("ax");
		LuaString axb = LuaString.valueOf("axb");
		LuaString axby = LuaString.valueOf("axby");
		LuaString xbya = LuaString.valueOf("xbya");
		LuaString bya = LuaString.valueOf("bya");
		LuaString xby = LuaString.valueOf("xby");
		LuaString axbya = LuaString.valueOf("axbya");
		LuaValue nil = LuaValue.NIL;

		assertEquals(nil, empty.invokemethod("match", args));
		assertEquals(nil, a.invokemethod("match", args));
		assertEquals(nil, ax.invokemethod("match", args));
		assertEquals(nil, axb.invokemethod("match", args));
		assertEquals(xby, axby.invokemethod("match", args));
		assertEquals(xby, xbya.invokemethod("match", args));
		assertEquals(nil, bya.invokemethod("match", args));
		assertEquals(xby, xby.invokemethod("match", args));
		assertEquals(xby, axbya.invokemethod("match", args));
		assertEquals(xby, axbya.substring(0, 4).invokemethod("match", args));
		assertEquals(nil, axbya.substring(0, 3).invokemethod("match", args));
		assertEquals(xby, axbya.substring(1, 5).invokemethod("match", args));
		assertEquals(nil, axbya.substring(2, 5).invokemethod("match", args));
	}

	@Test
	public void testMatchFrontierPatterns() {
		LuaValue[] args = { LuaString.valueOf("%f[%a]b"), LuaString.valueOf("x")  };

		LuaString a = LuaString.valueOf("a");
		LuaString ab = LuaString.valueOf("ab");
		LuaString ba = LuaString.valueOf("ba");
		LuaString xa = LuaString.valueOf("xa");
		LuaString bb = LuaString.valueOf("bb");
		LuaString xb = LuaString.valueOf("xb");

		LuaValue nil = LuaValue.NIL;

		assertEquals(a, a.invokemethod("gsub", args).arg1());
		assertEquals(ab, ab.invokemethod("gsub", args).arg1());
		assertEquals(xa, ba.invokemethod("gsub", args).arg1());
		assertEquals(xb, bb.invokemethod("gsub", args).arg1());
	}
}
