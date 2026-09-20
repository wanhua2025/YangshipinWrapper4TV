import { n as init_dist, r as __commonJSMin, t as global } from "./dist-UpsfCgKm.js";
//#region ../../../../node_modules/.pnpm/inherits@2.0.4/node_modules/inherits/inherits_browser.js
var require_inherits_browser = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	if (typeof Object.create === "function") module.exports = function inherits(ctor, superCtor) {
		if (superCtor) {
			ctor.super_ = superCtor;
			ctor.prototype = Object.create(superCtor.prototype, { constructor: {
				value: ctor,
				enumerable: false,
				writable: true,
				configurable: true
			} });
		}
	};
	else module.exports = function inherits(ctor, superCtor) {
		if (superCtor) {
			ctor.super_ = superCtor;
			var TempCtor = function() {};
			TempCtor.prototype = superCtor.prototype;
			ctor.prototype = new TempCtor();
			ctor.prototype.constructor = ctor;
		}
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/vite-plugin-node-polyfills@0.28.0_rollup@4.62.2_vite@8.0.16/node_modules/vite-plugin-node-polyfills/shims/buffer/dist/index.cjs
var require_dist = /* @__PURE__ */ __commonJSMin(((exports) => {
	Object.defineProperties(exports, {
		__esModule: { value: true },
		[Symbol.toStringTag]: { value: "Module" }
	});
	var buffer = {};
	var base64Js = {};
	base64Js.byteLength = byteLength;
	base64Js.toByteArray = toByteArray;
	base64Js.fromByteArray = fromByteArray;
	var lookup = [];
	var revLookup = [];
	var Arr = typeof Uint8Array !== "undefined" ? Uint8Array : Array;
	var code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	for (var i = 0, len = code.length; i < len; ++i) {
		lookup[i] = code[i];
		revLookup[code.charCodeAt(i)] = i;
	}
	revLookup["-".charCodeAt(0)] = 62;
	revLookup["_".charCodeAt(0)] = 63;
	function getLens(b64) {
		var len = b64.length;
		if (len % 4 > 0) throw new Error("Invalid string. Length must be a multiple of 4");
		var validLen = b64.indexOf("=");
		if (validLen === -1) validLen = len;
		var placeHoldersLen = validLen === len ? 0 : 4 - validLen % 4;
		return [validLen, placeHoldersLen];
	}
	function byteLength(b64) {
		var lens = getLens(b64);
		var validLen = lens[0];
		var placeHoldersLen = lens[1];
		return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
	}
	function _byteLength(b64, validLen, placeHoldersLen) {
		return (validLen + placeHoldersLen) * 3 / 4 - placeHoldersLen;
	}
	function toByteArray(b64) {
		var tmp;
		var lens = getLens(b64);
		var validLen = lens[0];
		var placeHoldersLen = lens[1];
		var arr = new Arr(_byteLength(b64, validLen, placeHoldersLen));
		var curByte = 0;
		var len = placeHoldersLen > 0 ? validLen - 4 : validLen;
		var i;
		for (i = 0; i < len; i += 4) {
			tmp = revLookup[b64.charCodeAt(i)] << 18 | revLookup[b64.charCodeAt(i + 1)] << 12 | revLookup[b64.charCodeAt(i + 2)] << 6 | revLookup[b64.charCodeAt(i + 3)];
			arr[curByte++] = tmp >> 16 & 255;
			arr[curByte++] = tmp >> 8 & 255;
			arr[curByte++] = tmp & 255;
		}
		if (placeHoldersLen === 2) {
			tmp = revLookup[b64.charCodeAt(i)] << 2 | revLookup[b64.charCodeAt(i + 1)] >> 4;
			arr[curByte++] = tmp & 255;
		}
		if (placeHoldersLen === 1) {
			tmp = revLookup[b64.charCodeAt(i)] << 10 | revLookup[b64.charCodeAt(i + 1)] << 4 | revLookup[b64.charCodeAt(i + 2)] >> 2;
			arr[curByte++] = tmp >> 8 & 255;
			arr[curByte++] = tmp & 255;
		}
		return arr;
	}
	function tripletToBase64(num) {
		return lookup[num >> 18 & 63] + lookup[num >> 12 & 63] + lookup[num >> 6 & 63] + lookup[num & 63];
	}
	function encodeChunk(uint8, start, end) {
		var tmp;
		var output = [];
		for (var i = start; i < end; i += 3) {
			tmp = (uint8[i] << 16 & 16711680) + (uint8[i + 1] << 8 & 65280) + (uint8[i + 2] & 255);
			output.push(tripletToBase64(tmp));
		}
		return output.join("");
	}
	function fromByteArray(uint8) {
		var tmp;
		var len = uint8.length;
		var extraBytes = len % 3;
		var parts = [];
		var maxChunkLength = 16383;
		for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) parts.push(encodeChunk(uint8, i, i + maxChunkLength > len2 ? len2 : i + maxChunkLength));
		if (extraBytes === 1) {
			tmp = uint8[len - 1];
			parts.push(lookup[tmp >> 2] + lookup[tmp << 4 & 63] + "==");
		} else if (extraBytes === 2) {
			tmp = (uint8[len - 2] << 8) + uint8[len - 1];
			parts.push(lookup[tmp >> 10] + lookup[tmp >> 4 & 63] + lookup[tmp << 2 & 63] + "=");
		}
		return parts.join("");
	}
	var ieee754 = {};
	/*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> */
	ieee754.read = function(buffer, offset, isLE, mLen, nBytes) {
		var e, m;
		var eLen = nBytes * 8 - mLen - 1;
		var eMax = (1 << eLen) - 1;
		var eBias = eMax >> 1;
		var nBits = -7;
		var i = isLE ? nBytes - 1 : 0;
		var d = isLE ? -1 : 1;
		var s = buffer[offset + i];
		i += d;
		e = s & (1 << -nBits) - 1;
		s >>= -nBits;
		nBits += eLen;
		for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8);
		m = e & (1 << -nBits) - 1;
		e >>= -nBits;
		nBits += mLen;
		for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8);
		if (e === 0) e = 1 - eBias;
		else if (e === eMax) return m ? NaN : (s ? -1 : 1) * Infinity;
		else {
			m = m + Math.pow(2, mLen);
			e = e - eBias;
		}
		return (s ? -1 : 1) * m * Math.pow(2, e - mLen);
	};
	ieee754.write = function(buffer, value, offset, isLE, mLen, nBytes) {
		var e, m, c;
		var eLen = nBytes * 8 - mLen - 1;
		var eMax = (1 << eLen) - 1;
		var eBias = eMax >> 1;
		var rt = mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0;
		var i = isLE ? 0 : nBytes - 1;
		var d = isLE ? 1 : -1;
		var s = value < 0 || value === 0 && 1 / value < 0 ? 1 : 0;
		value = Math.abs(value);
		if (isNaN(value) || value === Infinity) {
			m = isNaN(value) ? 1 : 0;
			e = eMax;
		} else {
			e = Math.floor(Math.log(value) / Math.LN2);
			if (value * (c = Math.pow(2, -e)) < 1) {
				e--;
				c *= 2;
			}
			if (e + eBias >= 1) value += rt / c;
			else value += rt * Math.pow(2, 1 - eBias);
			if (value * c >= 2) {
				e++;
				c /= 2;
			}
			if (e + eBias >= eMax) {
				m = 0;
				e = eMax;
			} else if (e + eBias >= 1) {
				m = (value * c - 1) * Math.pow(2, mLen);
				e = e + eBias;
			} else {
				m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen);
				e = 0;
			}
		}
		for (; mLen >= 8; buffer[offset + i] = m & 255, i += d, m /= 256, mLen -= 8);
		e = e << mLen | m;
		eLen += mLen;
		for (; eLen > 0; buffer[offset + i] = e & 255, i += d, e /= 256, eLen -= 8);
		buffer[offset + i - d] |= s * 128;
	};
	/*!
	* The buffer module from node.js, for the browser.
	*
	* @author   Feross Aboukhadijeh <https://feross.org>
	* @license  MIT
	*/
	(function(exports$1) {
		const base64 = base64Js;
		const ieee754$1 = ieee754;
		const customInspectSymbol = typeof Symbol === "function" && typeof Symbol["for"] === "function" ? Symbol["for"]("nodejs.util.inspect.custom") : null;
		exports$1.Buffer = Buffer;
		exports$1.SlowBuffer = SlowBuffer;
		exports$1.INSPECT_MAX_BYTES = 50;
		const K_MAX_LENGTH = 2147483647;
		exports$1.kMaxLength = K_MAX_LENGTH;
		const { Uint8Array: GlobalUint8Array, ArrayBuffer: GlobalArrayBuffer, SharedArrayBuffer: GlobalSharedArrayBuffer } = globalThis;
		/**
		* If `Buffer.TYPED_ARRAY_SUPPORT`:
		*   === true    Use Uint8Array implementation (fastest)
		*   === false   Print warning and recommend using `buffer` v4.x which has an Object
		*               implementation (most compatible, even IE6)
		*
		* Browsers that support typed arrays are IE 10+, Firefox 4+, Chrome 7+, Safari 5.1+,
		* Opera 11.6+, iOS 4.2+.
		*
		* We report that the browser does not support typed arrays if the are not subclassable
		* using __proto__. Firefox 4-29 lacks support for adding new properties to `Uint8Array`
		* (See: https://bugzilla.mozilla.org/show_bug.cgi?id=695438). IE 10 lacks support
		* for __proto__ and has a buggy typed array implementation.
		*/
		Buffer.TYPED_ARRAY_SUPPORT = typedArraySupport();
		if (!Buffer.TYPED_ARRAY_SUPPORT && typeof console !== "undefined" && typeof console.error === "function") console.error("This browser lacks typed array (Uint8Array) support which is required by `buffer` v5.x. Use `buffer` v4.x if you require old browser support.");
		function typedArraySupport() {
			try {
				const arr = new GlobalUint8Array(1);
				const proto = { foo: function() {
					return 42;
				} };
				Object.setPrototypeOf(proto, GlobalUint8Array.prototype);
				Object.setPrototypeOf(arr, proto);
				return arr.foo() === 42;
			} catch (e) {
				return false;
			}
		}
		Object.defineProperty(Buffer.prototype, "parent", {
			enumerable: true,
			get: function() {
				if (!Buffer.isBuffer(this)) return void 0;
				return this.buffer;
			}
		});
		Object.defineProperty(Buffer.prototype, "offset", {
			enumerable: true,
			get: function() {
				if (!Buffer.isBuffer(this)) return void 0;
				return this.byteOffset;
			}
		});
		function createBuffer(length) {
			if (length > K_MAX_LENGTH) throw new RangeError("The value \"" + length + "\" is invalid for option \"size\"");
			const buf = new GlobalUint8Array(length);
			Object.setPrototypeOf(buf, Buffer.prototype);
			return buf;
		}
		/**
		* The Buffer constructor returns instances of `Uint8Array` that have their
		* prototype changed to `Buffer.prototype`. Furthermore, `Buffer` is a subclass of
		* `Uint8Array`, so the returned instances will have all the node `Buffer` methods
		* and the `Uint8Array` methods. Square bracket notation works as expected -- it
		* returns a single octet.
		*
		* The `Uint8Array` prototype remains unmodified.
		*/
		function Buffer(arg, encodingOrOffset, length) {
			if (typeof arg === "number") {
				if (typeof encodingOrOffset === "string") throw new TypeError("The \"string\" argument must be of type string. Received type number");
				return allocUnsafe(arg);
			}
			return from(arg, encodingOrOffset, length);
		}
		Buffer.poolSize = 8192;
		function from(value, encodingOrOffset, length) {
			if (typeof value === "string") return fromString(value, encodingOrOffset);
			if (GlobalArrayBuffer.isView(value)) return fromArrayView(value);
			if (value == null) throw new TypeError("The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value);
			if (isInstance(value, GlobalArrayBuffer) || value && isInstance(value.buffer, GlobalArrayBuffer)) return fromArrayBuffer(value, encodingOrOffset, length);
			if (typeof GlobalSharedArrayBuffer !== "undefined" && (isInstance(value, GlobalSharedArrayBuffer) || value && isInstance(value.buffer, GlobalSharedArrayBuffer))) return fromArrayBuffer(value, encodingOrOffset, length);
			if (typeof value === "number") throw new TypeError("The \"value\" argument must not be of type number. Received type number");
			const valueOf = value.valueOf && value.valueOf();
			if (valueOf != null && valueOf !== value) return Buffer.from(valueOf, encodingOrOffset, length);
			const b = fromObject(value);
			if (b) return b;
			if (typeof Symbol !== "undefined" && Symbol.toPrimitive != null && typeof value[Symbol.toPrimitive] === "function") return Buffer.from(value[Symbol.toPrimitive]("string"), encodingOrOffset, length);
			throw new TypeError("The first argument must be one of type string, Buffer, ArrayBuffer, Array, or Array-like Object. Received type " + typeof value);
		}
		/**
		* Functionally equivalent to Buffer(arg, encoding) but throws a TypeError
		* if value is a number.
		* Buffer.from(str[, encoding])
		* Buffer.from(array)
		* Buffer.from(buffer)
		* Buffer.from(arrayBuffer[, byteOffset[, length]])
		**/
		Buffer.from = function(value, encodingOrOffset, length) {
			return from(value, encodingOrOffset, length);
		};
		Object.setPrototypeOf(Buffer.prototype, GlobalUint8Array.prototype);
		Object.setPrototypeOf(Buffer, GlobalUint8Array);
		function assertSize(size) {
			if (typeof size !== "number") throw new TypeError("\"size\" argument must be of type number");
			else if (size < 0) throw new RangeError("The value \"" + size + "\" is invalid for option \"size\"");
		}
		function alloc(size, fill, encoding) {
			assertSize(size);
			if (size <= 0) return createBuffer(size);
			if (fill !== void 0) return typeof encoding === "string" ? createBuffer(size).fill(fill, encoding) : createBuffer(size).fill(fill);
			return createBuffer(size);
		}
		/**
		* Creates a new filled Buffer instance.
		* alloc(size[, fill[, encoding]])
		**/
		Buffer.alloc = function(size, fill, encoding) {
			return alloc(size, fill, encoding);
		};
		function allocUnsafe(size) {
			assertSize(size);
			return createBuffer(size < 0 ? 0 : checked(size) | 0);
		}
		/**
		* Equivalent to Buffer(num), by default creates a non-zero-filled Buffer instance.
		* */
		Buffer.allocUnsafe = function(size) {
			return allocUnsafe(size);
		};
		/**
		* Equivalent to SlowBuffer(num), by default creates a non-zero-filled Buffer instance.
		*/
		Buffer.allocUnsafeSlow = function(size) {
			return allocUnsafe(size);
		};
		function fromString(string, encoding) {
			if (typeof encoding !== "string" || encoding === "") encoding = "utf8";
			if (!Buffer.isEncoding(encoding)) throw new TypeError("Unknown encoding: " + encoding);
			const length = byteLength(string, encoding) | 0;
			let buf = createBuffer(length);
			const actual = buf.write(string, encoding);
			if (actual !== length) buf = buf.slice(0, actual);
			return buf;
		}
		function fromArrayLike(array) {
			const length = array.length < 0 ? 0 : checked(array.length) | 0;
			const buf = createBuffer(length);
			for (let i = 0; i < length; i += 1) buf[i] = array[i] & 255;
			return buf;
		}
		function fromArrayView(arrayView) {
			if (isInstance(arrayView, GlobalUint8Array)) {
				const copy = new GlobalUint8Array(arrayView);
				return fromArrayBuffer(copy.buffer, copy.byteOffset, copy.byteLength);
			}
			return fromArrayLike(arrayView);
		}
		function fromArrayBuffer(array, byteOffset, length) {
			if (byteOffset < 0 || array.byteLength < byteOffset) throw new RangeError("\"offset\" is outside of buffer bounds");
			if (array.byteLength < byteOffset + (length || 0)) throw new RangeError("\"length\" is outside of buffer bounds");
			let buf;
			if (byteOffset === void 0 && length === void 0) buf = new GlobalUint8Array(array);
			else if (length === void 0) buf = new GlobalUint8Array(array, byteOffset);
			else buf = new GlobalUint8Array(array, byteOffset, length);
			Object.setPrototypeOf(buf, Buffer.prototype);
			return buf;
		}
		function fromObject(obj) {
			if (Buffer.isBuffer(obj)) {
				const len = checked(obj.length) | 0;
				const buf = createBuffer(len);
				if (buf.length === 0) return buf;
				obj.copy(buf, 0, 0, len);
				return buf;
			}
			if (obj.length !== void 0) {
				if (typeof obj.length !== "number" || numberIsNaN(obj.length)) return createBuffer(0);
				return fromArrayLike(obj);
			}
			if (obj.type === "Buffer" && Array.isArray(obj.data)) return fromArrayLike(obj.data);
		}
		function checked(length) {
			if (length >= K_MAX_LENGTH) throw new RangeError("Attempt to allocate Buffer larger than maximum size: 0x" + K_MAX_LENGTH.toString(16) + " bytes");
			return length | 0;
		}
		function SlowBuffer(length) {
			if (+length != length) length = 0;
			return Buffer.alloc(+length);
		}
		Buffer.isBuffer = function isBuffer(b) {
			return b != null && b._isBuffer === true && b !== Buffer.prototype;
		};
		Buffer.compare = function compare(a, b) {
			if (isInstance(a, GlobalUint8Array)) a = Buffer.from(a, a.offset, a.byteLength);
			if (isInstance(b, GlobalUint8Array)) b = Buffer.from(b, b.offset, b.byteLength);
			if (!Buffer.isBuffer(a) || !Buffer.isBuffer(b)) throw new TypeError("The \"buf1\", \"buf2\" arguments must be one of type Buffer or Uint8Array");
			if (a === b) return 0;
			let x = a.length;
			let y = b.length;
			for (let i = 0, len = Math.min(x, y); i < len; ++i) if (a[i] !== b[i]) {
				x = a[i];
				y = b[i];
				break;
			}
			if (x < y) return -1;
			if (y < x) return 1;
			return 0;
		};
		Buffer.isEncoding = function isEncoding(encoding) {
			switch (String(encoding).toLowerCase()) {
				case "hex":
				case "utf8":
				case "utf-8":
				case "ascii":
				case "latin1":
				case "binary":
				case "base64":
				case "ucs2":
				case "ucs-2":
				case "utf16le":
				case "utf-16le": return true;
				default: return false;
			}
		};
		Buffer.concat = function concat(list, length) {
			if (!Array.isArray(list)) throw new TypeError("\"list\" argument must be an Array of Buffers");
			if (list.length === 0) return Buffer.alloc(0);
			let i;
			if (length === void 0) {
				length = 0;
				for (i = 0; i < list.length; ++i) length += list[i].length;
			}
			const buffer = Buffer.allocUnsafe(length);
			let pos = 0;
			for (i = 0; i < list.length; ++i) {
				let buf = list[i];
				if (isInstance(buf, GlobalUint8Array)) if (pos + buf.length > buffer.length) {
					if (!Buffer.isBuffer(buf)) buf = Buffer.from(buf);
					buf.copy(buffer, pos);
				} else GlobalUint8Array.prototype.set.call(buffer, buf, pos);
				else if (!Buffer.isBuffer(buf)) throw new TypeError("\"list\" argument must be an Array of Buffers");
				else buf.copy(buffer, pos);
				pos += buf.length;
			}
			return buffer;
		};
		function byteLength(string, encoding) {
			if (Buffer.isBuffer(string)) return string.length;
			if (GlobalArrayBuffer.isView(string) || isInstance(string, GlobalArrayBuffer)) return string.byteLength;
			if (typeof string !== "string") throw new TypeError("The \"string\" argument must be one of type string, Buffer, or ArrayBuffer. Received type " + typeof string);
			const len = string.length;
			const mustMatch = arguments.length > 2 && arguments[2] === true;
			if (!mustMatch && len === 0) return 0;
			let loweredCase = false;
			for (;;) switch (encoding) {
				case "ascii":
				case "latin1":
				case "binary": return len;
				case "utf8":
				case "utf-8": return utf8ToBytes(string).length;
				case "ucs2":
				case "ucs-2":
				case "utf16le":
				case "utf-16le": return len * 2;
				case "hex": return len >>> 1;
				case "base64": return base64ToBytes(string).length;
				default:
					if (loweredCase) return mustMatch ? -1 : utf8ToBytes(string).length;
					encoding = ("" + encoding).toLowerCase();
					loweredCase = true;
			}
		}
		Buffer.byteLength = byteLength;
		function slowToString(encoding, start, end) {
			let loweredCase = false;
			if (start === void 0 || start < 0) start = 0;
			if (start > this.length) return "";
			if (end === void 0 || end > this.length) end = this.length;
			if (end <= 0) return "";
			end >>>= 0;
			start >>>= 0;
			if (end <= start) return "";
			if (!encoding) encoding = "utf8";
			while (true) switch (encoding) {
				case "hex": return hexSlice(this, start, end);
				case "utf8":
				case "utf-8": return utf8Slice(this, start, end);
				case "ascii": return asciiSlice(this, start, end);
				case "latin1":
				case "binary": return latin1Slice(this, start, end);
				case "base64": return base64Slice(this, start, end);
				case "ucs2":
				case "ucs-2":
				case "utf16le":
				case "utf-16le": return utf16leSlice(this, start, end);
				default:
					if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
					encoding = (encoding + "").toLowerCase();
					loweredCase = true;
			}
		}
		Buffer.prototype._isBuffer = true;
		function swap(b, n, m) {
			const i = b[n];
			b[n] = b[m];
			b[m] = i;
		}
		Buffer.prototype.swap16 = function swap16() {
			const len = this.length;
			if (len % 2 !== 0) throw new RangeError("Buffer size must be a multiple of 16-bits");
			for (let i = 0; i < len; i += 2) swap(this, i, i + 1);
			return this;
		};
		Buffer.prototype.swap32 = function swap32() {
			const len = this.length;
			if (len % 4 !== 0) throw new RangeError("Buffer size must be a multiple of 32-bits");
			for (let i = 0; i < len; i += 4) {
				swap(this, i, i + 3);
				swap(this, i + 1, i + 2);
			}
			return this;
		};
		Buffer.prototype.swap64 = function swap64() {
			const len = this.length;
			if (len % 8 !== 0) throw new RangeError("Buffer size must be a multiple of 64-bits");
			for (let i = 0; i < len; i += 8) {
				swap(this, i, i + 7);
				swap(this, i + 1, i + 6);
				swap(this, i + 2, i + 5);
				swap(this, i + 3, i + 4);
			}
			return this;
		};
		Buffer.prototype.toString = function toString() {
			const length = this.length;
			if (length === 0) return "";
			if (arguments.length === 0) return utf8Slice(this, 0, length);
			return slowToString.apply(this, arguments);
		};
		Buffer.prototype.toLocaleString = Buffer.prototype.toString;
		Buffer.prototype.equals = function equals(b) {
			if (!Buffer.isBuffer(b)) throw new TypeError("Argument must be a Buffer");
			if (this === b) return true;
			return Buffer.compare(this, b) === 0;
		};
		Buffer.prototype.inspect = function inspect() {
			let str = "";
			const max = exports$1.INSPECT_MAX_BYTES;
			str = this.toString("hex", 0, max).replace(/(.{2})/g, "$1 ").trim();
			if (this.length > max) str += " ... ";
			return "<Buffer " + str + ">";
		};
		if (customInspectSymbol) Buffer.prototype[customInspectSymbol] = Buffer.prototype.inspect;
		Buffer.prototype.compare = function compare(target, start, end, thisStart, thisEnd) {
			if (isInstance(target, GlobalUint8Array)) target = Buffer.from(target, target.offset, target.byteLength);
			if (!Buffer.isBuffer(target)) throw new TypeError("The \"target\" argument must be one of type Buffer or Uint8Array. Received type " + typeof target);
			if (start === void 0) start = 0;
			if (end === void 0) end = target ? target.length : 0;
			if (thisStart === void 0) thisStart = 0;
			if (thisEnd === void 0) thisEnd = this.length;
			if (start < 0 || end > target.length || thisStart < 0 || thisEnd > this.length) throw new RangeError("out of range index");
			if (thisStart >= thisEnd && start >= end) return 0;
			if (thisStart >= thisEnd) return -1;
			if (start >= end) return 1;
			start >>>= 0;
			end >>>= 0;
			thisStart >>>= 0;
			thisEnd >>>= 0;
			if (this === target) return 0;
			let x = thisEnd - thisStart;
			let y = end - start;
			const len = Math.min(x, y);
			const thisCopy = this.slice(thisStart, thisEnd);
			const targetCopy = target.slice(start, end);
			for (let i = 0; i < len; ++i) if (thisCopy[i] !== targetCopy[i]) {
				x = thisCopy[i];
				y = targetCopy[i];
				break;
			}
			if (x < y) return -1;
			if (y < x) return 1;
			return 0;
		};
		function bidirectionalIndexOf(buffer, val, byteOffset, encoding, dir) {
			if (buffer.length === 0) return -1;
			if (typeof byteOffset === "string") {
				encoding = byteOffset;
				byteOffset = 0;
			} else if (byteOffset > 2147483647) byteOffset = 2147483647;
			else if (byteOffset < -2147483648) byteOffset = -2147483648;
			byteOffset = +byteOffset;
			if (numberIsNaN(byteOffset)) byteOffset = dir ? 0 : buffer.length - 1;
			if (byteOffset < 0) byteOffset = buffer.length + byteOffset;
			if (byteOffset >= buffer.length) if (dir) return -1;
			else byteOffset = buffer.length - 1;
			else if (byteOffset < 0) if (dir) byteOffset = 0;
			else return -1;
			if (typeof val === "string") val = Buffer.from(val, encoding);
			if (Buffer.isBuffer(val)) {
				if (val.length === 0) return -1;
				return arrayIndexOf(buffer, val, byteOffset, encoding, dir);
			} else if (typeof val === "number") {
				val = val & 255;
				if (typeof GlobalUint8Array.prototype.indexOf === "function") if (dir) return GlobalUint8Array.prototype.indexOf.call(buffer, val, byteOffset);
				else return GlobalUint8Array.prototype.lastIndexOf.call(buffer, val, byteOffset);
				return arrayIndexOf(buffer, [val], byteOffset, encoding, dir);
			}
			throw new TypeError("val must be string, number or Buffer");
		}
		function arrayIndexOf(arr, val, byteOffset, encoding, dir) {
			let indexSize = 1;
			let arrLength = arr.length;
			let valLength = val.length;
			if (encoding !== void 0) {
				encoding = String(encoding).toLowerCase();
				if (encoding === "ucs2" || encoding === "ucs-2" || encoding === "utf16le" || encoding === "utf-16le") {
					if (arr.length < 2 || val.length < 2) return -1;
					indexSize = 2;
					arrLength /= 2;
					valLength /= 2;
					byteOffset /= 2;
				}
			}
			function read(buf, i) {
				if (indexSize === 1) return buf[i];
				else return buf.readUInt16BE(i * indexSize);
			}
			let i;
			if (dir) {
				let foundIndex = -1;
				for (i = byteOffset; i < arrLength; i++) if (read(arr, i) === read(val, foundIndex === -1 ? 0 : i - foundIndex)) {
					if (foundIndex === -1) foundIndex = i;
					if (i - foundIndex + 1 === valLength) return foundIndex * indexSize;
				} else {
					if (foundIndex !== -1) i -= i - foundIndex;
					foundIndex = -1;
				}
			} else {
				if (byteOffset + valLength > arrLength) byteOffset = arrLength - valLength;
				for (i = byteOffset; i >= 0; i--) {
					let found = true;
					for (let j = 0; j < valLength; j++) if (read(arr, i + j) !== read(val, j)) {
						found = false;
						break;
					}
					if (found) return i;
				}
			}
			return -1;
		}
		Buffer.prototype.includes = function includes(val, byteOffset, encoding) {
			return this.indexOf(val, byteOffset, encoding) !== -1;
		};
		Buffer.prototype.indexOf = function indexOf(val, byteOffset, encoding) {
			return bidirectionalIndexOf(this, val, byteOffset, encoding, true);
		};
		Buffer.prototype.lastIndexOf = function lastIndexOf(val, byteOffset, encoding) {
			return bidirectionalIndexOf(this, val, byteOffset, encoding, false);
		};
		function hexWrite(buf, string, offset, length) {
			offset = Number(offset) || 0;
			const remaining = buf.length - offset;
			if (!length) length = remaining;
			else {
				length = Number(length);
				if (length > remaining) length = remaining;
			}
			const strLen = string.length;
			if (length > strLen / 2) length = strLen / 2;
			let i;
			for (i = 0; i < length; ++i) {
				const parsed = parseInt(string.substr(i * 2, 2), 16);
				if (numberIsNaN(parsed)) return i;
				buf[offset + i] = parsed;
			}
			return i;
		}
		function utf8Write(buf, string, offset, length) {
			return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length);
		}
		function asciiWrite(buf, string, offset, length) {
			return blitBuffer(asciiToBytes(string), buf, offset, length);
		}
		function base64Write(buf, string, offset, length) {
			return blitBuffer(base64ToBytes(string), buf, offset, length);
		}
		function ucs2Write(buf, string, offset, length) {
			return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length);
		}
		Buffer.prototype.write = function write(string, offset, length, encoding) {
			if (offset === void 0) {
				encoding = "utf8";
				length = this.length;
				offset = 0;
			} else if (length === void 0 && typeof offset === "string") {
				encoding = offset;
				length = this.length;
				offset = 0;
			} else if (isFinite(offset)) {
				offset = offset >>> 0;
				if (isFinite(length)) {
					length = length >>> 0;
					if (encoding === void 0) encoding = "utf8";
				} else {
					encoding = length;
					length = void 0;
				}
			} else throw new Error("Buffer.write(string, encoding, offset[, length]) is no longer supported");
			const remaining = this.length - offset;
			if (length === void 0 || length > remaining) length = remaining;
			if (string.length > 0 && (length < 0 || offset < 0) || offset > this.length) throw new RangeError("Attempt to write outside buffer bounds");
			if (!encoding) encoding = "utf8";
			let loweredCase = false;
			for (;;) switch (encoding) {
				case "hex": return hexWrite(this, string, offset, length);
				case "utf8":
				case "utf-8": return utf8Write(this, string, offset, length);
				case "ascii":
				case "latin1":
				case "binary": return asciiWrite(this, string, offset, length);
				case "base64": return base64Write(this, string, offset, length);
				case "ucs2":
				case "ucs-2":
				case "utf16le":
				case "utf-16le": return ucs2Write(this, string, offset, length);
				default:
					if (loweredCase) throw new TypeError("Unknown encoding: " + encoding);
					encoding = ("" + encoding).toLowerCase();
					loweredCase = true;
			}
		};
		Buffer.prototype.toJSON = function toJSON() {
			return {
				type: "Buffer",
				data: Array.prototype.slice.call(this._arr || this, 0)
			};
		};
		function base64Slice(buf, start, end) {
			if (start === 0 && end === buf.length) return base64.fromByteArray(buf);
			else return base64.fromByteArray(buf.slice(start, end));
		}
		function utf8Slice(buf, start, end) {
			end = Math.min(buf.length, end);
			const res = [];
			let i = start;
			while (i < end) {
				const firstByte = buf[i];
				let codePoint = null;
				let bytesPerSequence = firstByte > 239 ? 4 : firstByte > 223 ? 3 : firstByte > 191 ? 2 : 1;
				if (i + bytesPerSequence <= end) {
					let secondByte, thirdByte, fourthByte, tempCodePoint;
					switch (bytesPerSequence) {
						case 1:
							if (firstByte < 128) codePoint = firstByte;
							break;
						case 2:
							secondByte = buf[i + 1];
							if ((secondByte & 192) === 128) {
								tempCodePoint = (firstByte & 31) << 6 | secondByte & 63;
								if (tempCodePoint > 127) codePoint = tempCodePoint;
							}
							break;
						case 3:
							secondByte = buf[i + 1];
							thirdByte = buf[i + 2];
							if ((secondByte & 192) === 128 && (thirdByte & 192) === 128) {
								tempCodePoint = (firstByte & 15) << 12 | (secondByte & 63) << 6 | thirdByte & 63;
								if (tempCodePoint > 2047 && (tempCodePoint < 55296 || tempCodePoint > 57343)) codePoint = tempCodePoint;
							}
							break;
						case 4:
							secondByte = buf[i + 1];
							thirdByte = buf[i + 2];
							fourthByte = buf[i + 3];
							if ((secondByte & 192) === 128 && (thirdByte & 192) === 128 && (fourthByte & 192) === 128) {
								tempCodePoint = (firstByte & 15) << 18 | (secondByte & 63) << 12 | (thirdByte & 63) << 6 | fourthByte & 63;
								if (tempCodePoint > 65535 && tempCodePoint < 1114112) codePoint = tempCodePoint;
							}
					}
				}
				if (codePoint === null) {
					codePoint = 65533;
					bytesPerSequence = 1;
				} else if (codePoint > 65535) {
					codePoint -= 65536;
					res.push(codePoint >>> 10 & 1023 | 55296);
					codePoint = 56320 | codePoint & 1023;
				}
				res.push(codePoint);
				i += bytesPerSequence;
			}
			return decodeCodePointsArray(res);
		}
		const MAX_ARGUMENTS_LENGTH = 4096;
		function decodeCodePointsArray(codePoints) {
			const len = codePoints.length;
			if (len <= MAX_ARGUMENTS_LENGTH) return String.fromCharCode.apply(String, codePoints);
			let res = "";
			let i = 0;
			while (i < len) res += String.fromCharCode.apply(String, codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH));
			return res;
		}
		function asciiSlice(buf, start, end) {
			let ret = "";
			end = Math.min(buf.length, end);
			for (let i = start; i < end; ++i) ret += String.fromCharCode(buf[i] & 127);
			return ret;
		}
		function latin1Slice(buf, start, end) {
			let ret = "";
			end = Math.min(buf.length, end);
			for (let i = start; i < end; ++i) ret += String.fromCharCode(buf[i]);
			return ret;
		}
		function hexSlice(buf, start, end) {
			const len = buf.length;
			if (!start || start < 0) start = 0;
			if (!end || end < 0 || end > len) end = len;
			let out = "";
			for (let i = start; i < end; ++i) out += hexSliceLookupTable[buf[i]];
			return out;
		}
		function utf16leSlice(buf, start, end) {
			const bytes = buf.slice(start, end);
			let res = "";
			for (let i = 0; i < bytes.length - 1; i += 2) res += String.fromCharCode(bytes[i] + bytes[i + 1] * 256);
			return res;
		}
		Buffer.prototype.slice = function slice(start, end) {
			const len = this.length;
			start = ~~start;
			end = end === void 0 ? len : ~~end;
			if (start < 0) {
				start += len;
				if (start < 0) start = 0;
			} else if (start > len) start = len;
			if (end < 0) {
				end += len;
				if (end < 0) end = 0;
			} else if (end > len) end = len;
			if (end < start) end = start;
			const newBuf = this.subarray(start, end);
			Object.setPrototypeOf(newBuf, Buffer.prototype);
			return newBuf;
		};
		function checkOffset(offset, ext, length) {
			if (offset % 1 !== 0 || offset < 0) throw new RangeError("offset is not uint");
			if (offset + ext > length) throw new RangeError("Trying to access beyond buffer length");
		}
		Buffer.prototype.readUintLE = Buffer.prototype.readUIntLE = function readUIntLE(offset, byteLength, noAssert) {
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) checkOffset(offset, byteLength, this.length);
			let val = this[offset];
			let mul = 1;
			let i = 0;
			while (++i < byteLength && (mul *= 256)) val += this[offset + i] * mul;
			return val;
		};
		Buffer.prototype.readUintBE = Buffer.prototype.readUIntBE = function readUIntBE(offset, byteLength, noAssert) {
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) checkOffset(offset, byteLength, this.length);
			let val = this[offset + --byteLength];
			let mul = 1;
			while (byteLength > 0 && (mul *= 256)) val += this[offset + --byteLength] * mul;
			return val;
		};
		Buffer.prototype.readUint8 = Buffer.prototype.readUInt8 = function readUInt8(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 1, this.length);
			return this[offset];
		};
		Buffer.prototype.readUint16LE = Buffer.prototype.readUInt16LE = function readUInt16LE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 2, this.length);
			return this[offset] | this[offset + 1] << 8;
		};
		Buffer.prototype.readUint16BE = Buffer.prototype.readUInt16BE = function readUInt16BE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 2, this.length);
			return this[offset] << 8 | this[offset + 1];
		};
		Buffer.prototype.readUint32LE = Buffer.prototype.readUInt32LE = function readUInt32LE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return (this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16) + this[offset + 3] * 16777216;
		};
		Buffer.prototype.readUint32BE = Buffer.prototype.readUInt32BE = function readUInt32BE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return this[offset] * 16777216 + (this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3]);
		};
		Buffer.prototype.readBigUInt64LE = defineBigIntMethod(function readBigUInt64LE(offset) {
			offset = offset >>> 0;
			validateNumber(offset, "offset");
			const first = this[offset];
			const last = this[offset + 7];
			if (first === void 0 || last === void 0) boundsError(offset, this.length - 8);
			const lo = first + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24;
			const hi = this[++offset] + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + last * 2 ** 24;
			return BigInt(lo) + (BigInt(hi) << BigInt(32));
		});
		Buffer.prototype.readBigUInt64BE = defineBigIntMethod(function readBigUInt64BE(offset) {
			offset = offset >>> 0;
			validateNumber(offset, "offset");
			const first = this[offset];
			const last = this[offset + 7];
			if (first === void 0 || last === void 0) boundsError(offset, this.length - 8);
			const hi = first * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
			const lo = this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last;
			return (BigInt(hi) << BigInt(32)) + BigInt(lo);
		});
		Buffer.prototype.readIntLE = function readIntLE(offset, byteLength, noAssert) {
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) checkOffset(offset, byteLength, this.length);
			let val = this[offset];
			let mul = 1;
			let i = 0;
			while (++i < byteLength && (mul *= 256)) val += this[offset + i] * mul;
			mul *= 128;
			if (val >= mul) val -= Math.pow(2, 8 * byteLength);
			return val;
		};
		Buffer.prototype.readIntBE = function readIntBE(offset, byteLength, noAssert) {
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) checkOffset(offset, byteLength, this.length);
			let i = byteLength;
			let mul = 1;
			let val = this[offset + --i];
			while (i > 0 && (mul *= 256)) val += this[offset + --i] * mul;
			mul *= 128;
			if (val >= mul) val -= Math.pow(2, 8 * byteLength);
			return val;
		};
		Buffer.prototype.readInt8 = function readInt8(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 1, this.length);
			if (!(this[offset] & 128)) return this[offset];
			return (255 - this[offset] + 1) * -1;
		};
		Buffer.prototype.readInt16LE = function readInt16LE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 2, this.length);
			const val = this[offset] | this[offset + 1] << 8;
			return val & 32768 ? val | 4294901760 : val;
		};
		Buffer.prototype.readInt16BE = function readInt16BE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 2, this.length);
			const val = this[offset + 1] | this[offset] << 8;
			return val & 32768 ? val | 4294901760 : val;
		};
		Buffer.prototype.readInt32LE = function readInt32LE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return this[offset] | this[offset + 1] << 8 | this[offset + 2] << 16 | this[offset + 3] << 24;
		};
		Buffer.prototype.readInt32BE = function readInt32BE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return this[offset] << 24 | this[offset + 1] << 16 | this[offset + 2] << 8 | this[offset + 3];
		};
		Buffer.prototype.readBigInt64LE = defineBigIntMethod(function readBigInt64LE(offset) {
			offset = offset >>> 0;
			validateNumber(offset, "offset");
			const first = this[offset];
			const last = this[offset + 7];
			if (first === void 0 || last === void 0) boundsError(offset, this.length - 8);
			const val = this[offset + 4] + this[offset + 5] * 2 ** 8 + this[offset + 6] * 2 ** 16 + (last << 24);
			return (BigInt(val) << BigInt(32)) + BigInt(first + this[++offset] * 2 ** 8 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 24);
		});
		Buffer.prototype.readBigInt64BE = defineBigIntMethod(function readBigInt64BE(offset) {
			offset = offset >>> 0;
			validateNumber(offset, "offset");
			const first = this[offset];
			const last = this[offset + 7];
			if (first === void 0 || last === void 0) boundsError(offset, this.length - 8);
			const val = (first << 24) + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + this[++offset];
			return (BigInt(val) << BigInt(32)) + BigInt(this[++offset] * 2 ** 24 + this[++offset] * 2 ** 16 + this[++offset] * 2 ** 8 + last);
		});
		Buffer.prototype.readFloatLE = function readFloatLE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return ieee754$1.read(this, offset, true, 23, 4);
		};
		Buffer.prototype.readFloatBE = function readFloatBE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 4, this.length);
			return ieee754$1.read(this, offset, false, 23, 4);
		};
		Buffer.prototype.readDoubleLE = function readDoubleLE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 8, this.length);
			return ieee754$1.read(this, offset, true, 52, 8);
		};
		Buffer.prototype.readDoubleBE = function readDoubleBE(offset, noAssert) {
			offset = offset >>> 0;
			if (!noAssert) checkOffset(offset, 8, this.length);
			return ieee754$1.read(this, offset, false, 52, 8);
		};
		function checkInt(buf, value, offset, ext, max, min) {
			if (!Buffer.isBuffer(buf)) throw new TypeError("\"buffer\" argument must be a Buffer instance");
			if (value > max || value < min) throw new RangeError("\"value\" argument is out of bounds");
			if (offset + ext > buf.length) throw new RangeError("Index out of range");
		}
		Buffer.prototype.writeUintLE = Buffer.prototype.writeUIntLE = function writeUIntLE(value, offset, byteLength, noAssert) {
			value = +value;
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) {
				const maxBytes = Math.pow(2, 8 * byteLength) - 1;
				checkInt(this, value, offset, byteLength, maxBytes, 0);
			}
			let mul = 1;
			let i = 0;
			this[offset] = value & 255;
			while (++i < byteLength && (mul *= 256)) this[offset + i] = value / mul & 255;
			return offset + byteLength;
		};
		Buffer.prototype.writeUintBE = Buffer.prototype.writeUIntBE = function writeUIntBE(value, offset, byteLength, noAssert) {
			value = +value;
			offset = offset >>> 0;
			byteLength = byteLength >>> 0;
			if (!noAssert) {
				const maxBytes = Math.pow(2, 8 * byteLength) - 1;
				checkInt(this, value, offset, byteLength, maxBytes, 0);
			}
			let i = byteLength - 1;
			let mul = 1;
			this[offset + i] = value & 255;
			while (--i >= 0 && (mul *= 256)) this[offset + i] = value / mul & 255;
			return offset + byteLength;
		};
		Buffer.prototype.writeUint8 = Buffer.prototype.writeUInt8 = function writeUInt8(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 1, 255, 0);
			this[offset] = value & 255;
			return offset + 1;
		};
		Buffer.prototype.writeUint16LE = Buffer.prototype.writeUInt16LE = function writeUInt16LE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
			this[offset] = value & 255;
			this[offset + 1] = value >>> 8;
			return offset + 2;
		};
		Buffer.prototype.writeUint16BE = Buffer.prototype.writeUInt16BE = function writeUInt16BE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 2, 65535, 0);
			this[offset] = value >>> 8;
			this[offset + 1] = value & 255;
			return offset + 2;
		};
		Buffer.prototype.writeUint32LE = Buffer.prototype.writeUInt32LE = function writeUInt32LE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
			this[offset + 3] = value >>> 24;
			this[offset + 2] = value >>> 16;
			this[offset + 1] = value >>> 8;
			this[offset] = value & 255;
			return offset + 4;
		};
		Buffer.prototype.writeUint32BE = Buffer.prototype.writeUInt32BE = function writeUInt32BE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 4, 4294967295, 0);
			this[offset] = value >>> 24;
			this[offset + 1] = value >>> 16;
			this[offset + 2] = value >>> 8;
			this[offset + 3] = value & 255;
			return offset + 4;
		};
		function wrtBigUInt64LE(buf, value, offset, min, max) {
			checkIntBI(value, min, max, buf, offset, 7);
			let lo = Number(value & BigInt(4294967295));
			buf[offset++] = lo;
			lo = lo >> 8;
			buf[offset++] = lo;
			lo = lo >> 8;
			buf[offset++] = lo;
			lo = lo >> 8;
			buf[offset++] = lo;
			let hi = Number(value >> BigInt(32) & BigInt(4294967295));
			buf[offset++] = hi;
			hi = hi >> 8;
			buf[offset++] = hi;
			hi = hi >> 8;
			buf[offset++] = hi;
			hi = hi >> 8;
			buf[offset++] = hi;
			return offset;
		}
		function wrtBigUInt64BE(buf, value, offset, min, max) {
			checkIntBI(value, min, max, buf, offset, 7);
			let lo = Number(value & BigInt(4294967295));
			buf[offset + 7] = lo;
			lo = lo >> 8;
			buf[offset + 6] = lo;
			lo = lo >> 8;
			buf[offset + 5] = lo;
			lo = lo >> 8;
			buf[offset + 4] = lo;
			let hi = Number(value >> BigInt(32) & BigInt(4294967295));
			buf[offset + 3] = hi;
			hi = hi >> 8;
			buf[offset + 2] = hi;
			hi = hi >> 8;
			buf[offset + 1] = hi;
			hi = hi >> 8;
			buf[offset] = hi;
			return offset + 8;
		}
		Buffer.prototype.writeBigUInt64LE = defineBigIntMethod(function writeBigUInt64LE(value, offset = 0) {
			return wrtBigUInt64LE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
		});
		Buffer.prototype.writeBigUInt64BE = defineBigIntMethod(function writeBigUInt64BE(value, offset = 0) {
			return wrtBigUInt64BE(this, value, offset, BigInt(0), BigInt("0xffffffffffffffff"));
		});
		Buffer.prototype.writeIntLE = function writeIntLE(value, offset, byteLength, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) {
				const limit = Math.pow(2, 8 * byteLength - 1);
				checkInt(this, value, offset, byteLength, limit - 1, -limit);
			}
			let i = 0;
			let mul = 1;
			let sub = 0;
			this[offset] = value & 255;
			while (++i < byteLength && (mul *= 256)) {
				if (value < 0 && sub === 0 && this[offset + i - 1] !== 0) sub = 1;
				this[offset + i] = (value / mul >> 0) - sub & 255;
			}
			return offset + byteLength;
		};
		Buffer.prototype.writeIntBE = function writeIntBE(value, offset, byteLength, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) {
				const limit = Math.pow(2, 8 * byteLength - 1);
				checkInt(this, value, offset, byteLength, limit - 1, -limit);
			}
			let i = byteLength - 1;
			let mul = 1;
			let sub = 0;
			this[offset + i] = value & 255;
			while (--i >= 0 && (mul *= 256)) {
				if (value < 0 && sub === 0 && this[offset + i + 1] !== 0) sub = 1;
				this[offset + i] = (value / mul >> 0) - sub & 255;
			}
			return offset + byteLength;
		};
		Buffer.prototype.writeInt8 = function writeInt8(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 1, 127, -128);
			if (value < 0) value = 255 + value + 1;
			this[offset] = value & 255;
			return offset + 1;
		};
		Buffer.prototype.writeInt16LE = function writeInt16LE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
			this[offset] = value & 255;
			this[offset + 1] = value >>> 8;
			return offset + 2;
		};
		Buffer.prototype.writeInt16BE = function writeInt16BE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 2, 32767, -32768);
			this[offset] = value >>> 8;
			this[offset + 1] = value & 255;
			return offset + 2;
		};
		Buffer.prototype.writeInt32LE = function writeInt32LE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
			this[offset] = value & 255;
			this[offset + 1] = value >>> 8;
			this[offset + 2] = value >>> 16;
			this[offset + 3] = value >>> 24;
			return offset + 4;
		};
		Buffer.prototype.writeInt32BE = function writeInt32BE(value, offset, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkInt(this, value, offset, 4, 2147483647, -2147483648);
			if (value < 0) value = 4294967295 + value + 1;
			this[offset] = value >>> 24;
			this[offset + 1] = value >>> 16;
			this[offset + 2] = value >>> 8;
			this[offset + 3] = value & 255;
			return offset + 4;
		};
		Buffer.prototype.writeBigInt64LE = defineBigIntMethod(function writeBigInt64LE(value, offset = 0) {
			return wrtBigUInt64LE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
		});
		Buffer.prototype.writeBigInt64BE = defineBigIntMethod(function writeBigInt64BE(value, offset = 0) {
			return wrtBigUInt64BE(this, value, offset, -BigInt("0x8000000000000000"), BigInt("0x7fffffffffffffff"));
		});
		function checkIEEE754(buf, value, offset, ext, max, min) {
			if (offset + ext > buf.length) throw new RangeError("Index out of range");
			if (offset < 0) throw new RangeError("Index out of range");
		}
		function writeFloat(buf, value, offset, littleEndian, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkIEEE754(buf, value, offset, 4);
			ieee754$1.write(buf, value, offset, littleEndian, 23, 4);
			return offset + 4;
		}
		Buffer.prototype.writeFloatLE = function writeFloatLE(value, offset, noAssert) {
			return writeFloat(this, value, offset, true, noAssert);
		};
		Buffer.prototype.writeFloatBE = function writeFloatBE(value, offset, noAssert) {
			return writeFloat(this, value, offset, false, noAssert);
		};
		function writeDouble(buf, value, offset, littleEndian, noAssert) {
			value = +value;
			offset = offset >>> 0;
			if (!noAssert) checkIEEE754(buf, value, offset, 8);
			ieee754$1.write(buf, value, offset, littleEndian, 52, 8);
			return offset + 8;
		}
		Buffer.prototype.writeDoubleLE = function writeDoubleLE(value, offset, noAssert) {
			return writeDouble(this, value, offset, true, noAssert);
		};
		Buffer.prototype.writeDoubleBE = function writeDoubleBE(value, offset, noAssert) {
			return writeDouble(this, value, offset, false, noAssert);
		};
		Buffer.prototype.copy = function copy(target, targetStart, start, end) {
			if (!Buffer.isBuffer(target)) throw new TypeError("argument should be a Buffer");
			if (!start) start = 0;
			if (!end && end !== 0) end = this.length;
			if (targetStart >= target.length) targetStart = target.length;
			if (!targetStart) targetStart = 0;
			if (end > 0 && end < start) end = start;
			if (end === start) return 0;
			if (target.length === 0 || this.length === 0) return 0;
			if (targetStart < 0) throw new RangeError("targetStart out of bounds");
			if (start < 0 || start >= this.length) throw new RangeError("Index out of range");
			if (end < 0) throw new RangeError("sourceEnd out of bounds");
			if (end > this.length) end = this.length;
			if (target.length - targetStart < end - start) end = target.length - targetStart + start;
			const len = end - start;
			if (this === target && typeof GlobalUint8Array.prototype.copyWithin === "function") this.copyWithin(targetStart, start, end);
			else GlobalUint8Array.prototype.set.call(target, this.subarray(start, end), targetStart);
			return len;
		};
		Buffer.prototype.fill = function fill(val, start, end, encoding) {
			if (typeof val === "string") {
				if (typeof start === "string") {
					encoding = start;
					start = 0;
					end = this.length;
				} else if (typeof end === "string") {
					encoding = end;
					end = this.length;
				}
				if (encoding !== void 0 && typeof encoding !== "string") throw new TypeError("encoding must be a string");
				if (typeof encoding === "string" && !Buffer.isEncoding(encoding)) throw new TypeError("Unknown encoding: " + encoding);
				if (val.length === 1) {
					const code = val.charCodeAt(0);
					if (encoding === "utf8" && code < 128 || encoding === "latin1") val = code;
				}
			} else if (typeof val === "number") val = val & 255;
			else if (typeof val === "boolean") val = Number(val);
			if (start < 0 || this.length < start || this.length < end) throw new RangeError("Out of range index");
			if (end <= start) return this;
			start = start >>> 0;
			end = end === void 0 ? this.length : end >>> 0;
			if (!val) val = 0;
			let i;
			if (typeof val === "number") for (i = start; i < end; ++i) this[i] = val;
			else {
				const bytes = Buffer.isBuffer(val) ? val : Buffer.from(val, encoding);
				const len = bytes.length;
				if (len === 0) throw new TypeError("The value \"" + val + "\" is invalid for argument \"value\"");
				for (i = 0; i < end - start; ++i) this[i + start] = bytes[i % len];
			}
			return this;
		};
		const errors = {};
		function E(sym, getMessage, Base) {
			errors[sym] = class NodeError extends Base {
				constructor() {
					super();
					Object.defineProperty(this, "message", {
						value: getMessage.apply(this, arguments),
						writable: true,
						configurable: true
					});
					this.name = `${this.name} [${sym}]`;
					this.stack;
					delete this.name;
				}
				get code() {
					return sym;
				}
				set code(value) {
					Object.defineProperty(this, "code", {
						configurable: true,
						enumerable: true,
						value,
						writable: true
					});
				}
				toString() {
					return `${this.name} [${sym}]: ${this.message}`;
				}
			};
		}
		E("ERR_BUFFER_OUT_OF_BOUNDS", function(name) {
			if (name) return `${name} is outside of buffer bounds`;
			return "Attempt to access memory outside buffer bounds";
		}, RangeError);
		E("ERR_INVALID_ARG_TYPE", function(name, actual) {
			return `The "${name}" argument must be of type number. Received type ${typeof actual}`;
		}, TypeError);
		E("ERR_OUT_OF_RANGE", function(str, range, input) {
			let msg = `The value of "${str}" is out of range.`;
			let received = input;
			if (Number.isInteger(input) && Math.abs(input) > 2 ** 32) received = addNumericalSeparator(String(input));
			else if (typeof input === "bigint") {
				received = String(input);
				if (input > BigInt(2) ** BigInt(32) || input < -(BigInt(2) ** BigInt(32))) received = addNumericalSeparator(received);
				received += "n";
			}
			msg += ` It must be ${range}. Received ${received}`;
			return msg;
		}, RangeError);
		function addNumericalSeparator(val) {
			let res = "";
			let i = val.length;
			const start = val[0] === "-" ? 1 : 0;
			for (; i >= start + 4; i -= 3) res = `_${val.slice(i - 3, i)}${res}`;
			return `${val.slice(0, i)}${res}`;
		}
		function checkBounds(buf, offset, byteLength) {
			validateNumber(offset, "offset");
			if (buf[offset] === void 0 || buf[offset + byteLength] === void 0) boundsError(offset, buf.length - (byteLength + 1));
		}
		function checkIntBI(value, min, max, buf, offset, byteLength) {
			if (value > max || value < min) {
				const n = typeof min === "bigint" ? "n" : "";
				let range;
				if (byteLength > 3) if (min === 0 || min === BigInt(0)) range = `>= 0${n} and < 2${n} ** ${(byteLength + 1) * 8}${n}`;
				else range = `>= -(2${n} ** ${(byteLength + 1) * 8 - 1}${n}) and < 2 ** ${(byteLength + 1) * 8 - 1}${n}`;
				else range = `>= ${min}${n} and <= ${max}${n}`;
				throw new errors.ERR_OUT_OF_RANGE("value", range, value);
			}
			checkBounds(buf, offset, byteLength);
		}
		function validateNumber(value, name) {
			if (typeof value !== "number") throw new errors.ERR_INVALID_ARG_TYPE(name, "number", value);
		}
		function boundsError(value, length, type) {
			if (Math.floor(value) !== value) {
				validateNumber(value, type);
				throw new errors.ERR_OUT_OF_RANGE(type || "offset", "an integer", value);
			}
			if (length < 0) throw new errors.ERR_BUFFER_OUT_OF_BOUNDS();
			throw new errors.ERR_OUT_OF_RANGE(type || "offset", `>= ${type ? 1 : 0} and <= ${length}`, value);
		}
		const INVALID_BASE64_RE = /[^+/0-9A-Za-z-_]/g;
		function base64clean(str) {
			str = str.split("=")[0];
			str = str.trim().replace(INVALID_BASE64_RE, "");
			if (str.length < 2) return "";
			while (str.length % 4 !== 0) str = str + "=";
			return str;
		}
		function utf8ToBytes(string, units) {
			units = units || Infinity;
			let codePoint;
			const length = string.length;
			let leadSurrogate = null;
			const bytes = [];
			for (let i = 0; i < length; ++i) {
				codePoint = string.charCodeAt(i);
				if (codePoint > 55295 && codePoint < 57344) {
					if (!leadSurrogate) {
						if (codePoint > 56319) {
							if ((units -= 3) > -1) bytes.push(239, 191, 189);
							continue;
						} else if (i + 1 === length) {
							if ((units -= 3) > -1) bytes.push(239, 191, 189);
							continue;
						}
						leadSurrogate = codePoint;
						continue;
					}
					if (codePoint < 56320) {
						if ((units -= 3) > -1) bytes.push(239, 191, 189);
						leadSurrogate = codePoint;
						continue;
					}
					codePoint = (leadSurrogate - 55296 << 10 | codePoint - 56320) + 65536;
				} else if (leadSurrogate) {
					if ((units -= 3) > -1) bytes.push(239, 191, 189);
				}
				leadSurrogate = null;
				if (codePoint < 128) {
					if ((units -= 1) < 0) break;
					bytes.push(codePoint);
				} else if (codePoint < 2048) {
					if ((units -= 2) < 0) break;
					bytes.push(codePoint >> 6 | 192, codePoint & 63 | 128);
				} else if (codePoint < 65536) {
					if ((units -= 3) < 0) break;
					bytes.push(codePoint >> 12 | 224, codePoint >> 6 & 63 | 128, codePoint & 63 | 128);
				} else if (codePoint < 1114112) {
					if ((units -= 4) < 0) break;
					bytes.push(codePoint >> 18 | 240, codePoint >> 12 & 63 | 128, codePoint >> 6 & 63 | 128, codePoint & 63 | 128);
				} else throw new Error("Invalid code point");
			}
			return bytes;
		}
		function asciiToBytes(str) {
			const byteArray = [];
			for (let i = 0; i < str.length; ++i) byteArray.push(str.charCodeAt(i) & 255);
			return byteArray;
		}
		function utf16leToBytes(str, units) {
			let c, hi, lo;
			const byteArray = [];
			for (let i = 0; i < str.length; ++i) {
				if ((units -= 2) < 0) break;
				c = str.charCodeAt(i);
				hi = c >> 8;
				lo = c % 256;
				byteArray.push(lo);
				byteArray.push(hi);
			}
			return byteArray;
		}
		function base64ToBytes(str) {
			return base64.toByteArray(base64clean(str));
		}
		function blitBuffer(src, dst, offset, length) {
			let i;
			for (i = 0; i < length; ++i) {
				if (i + offset >= dst.length || i >= src.length) break;
				dst[i + offset] = src[i];
			}
			return i;
		}
		function isInstance(obj, type) {
			return obj instanceof type || obj != null && obj.constructor != null && obj.constructor.name != null && obj.constructor.name === type.name;
		}
		function numberIsNaN(obj) {
			return obj !== obj;
		}
		const hexSliceLookupTable = (function() {
			const alphabet = "0123456789abcdef";
			const table = new Array(256);
			for (let i = 0; i < 16; ++i) {
				const i16 = i * 16;
				for (let j = 0; j < 16; ++j) table[i16 + j] = alphabet[i] + alphabet[j];
			}
			return table;
		})();
		function defineBigIntMethod(fn) {
			return typeof BigInt === "undefined" ? BufferBigIntNotDefined : fn;
		}
		function BufferBigIntNotDefined() {
			throw new Error("BigInt not supported");
		}
	})(buffer);
	var Buffer = buffer.Buffer;
	exports.Blob = buffer.Blob;
	exports.BlobOptions = buffer.BlobOptions;
	exports.Buffer = buffer.Buffer;
	exports.File = buffer.File;
	exports.FileOptions = buffer.FileOptions;
	exports.INSPECT_MAX_BYTES = buffer.INSPECT_MAX_BYTES;
	exports.SlowBuffer = buffer.SlowBuffer;
	exports.TranscodeEncoding = buffer.TranscodeEncoding;
	exports.atob = buffer.atob;
	exports.btoa = buffer.btoa;
	exports.constants = buffer.constants;
	exports.default = Buffer;
	exports.isAscii = buffer.isAscii;
	exports.isUtf8 = buffer.isUtf8;
	exports.kMaxLength = buffer.kMaxLength;
	exports.kStringMaxLength = buffer.kStringMaxLength;
	exports.resolveObjectURL = buffer.resolveObjectURL;
	exports.transcode = buffer.transcode;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/safe-buffer@5.2.1/node_modules/safe-buffer/index.js
var require_safe_buffer = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/*! safe-buffer. MIT License. Feross Aboukhadijeh <https://feross.org/opensource> */
	var buffer = require_dist();
	var Buffer = buffer.Buffer;
	function copyProps(src, dst) {
		for (var key in src) dst[key] = src[key];
	}
	if (Buffer.from && Buffer.alloc && Buffer.allocUnsafe && Buffer.allocUnsafeSlow) module.exports = buffer;
	else {
		copyProps(buffer, exports);
		exports.Buffer = SafeBuffer;
	}
	function SafeBuffer(arg, encodingOrOffset, length) {
		return Buffer(arg, encodingOrOffset, length);
	}
	SafeBuffer.prototype = Object.create(Buffer.prototype);
	copyProps(Buffer, SafeBuffer);
	SafeBuffer.from = function(arg, encodingOrOffset, length) {
		if (typeof arg === "number") throw new TypeError("Argument must not be a number");
		return Buffer(arg, encodingOrOffset, length);
	};
	SafeBuffer.alloc = function(size, fill, encoding) {
		if (typeof size !== "number") throw new TypeError("Argument must be a number");
		var buf = Buffer(size);
		if (fill !== void 0) if (typeof encoding === "string") buf.fill(fill, encoding);
		else buf.fill(fill);
		else buf.fill(0);
		return buf;
	};
	SafeBuffer.allocUnsafe = function(size) {
		if (typeof size !== "number") throw new TypeError("Argument must be a number");
		return Buffer(size);
	};
	SafeBuffer.allocUnsafeSlow = function(size) {
		if (typeof size !== "number") throw new TypeError("Argument must be a number");
		return buffer.SlowBuffer(size);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/isarray@2.0.5/node_modules/isarray/index.js
var require_isarray = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var toString = {}.toString;
	module.exports = Array.isArray || function(arr) {
		return toString.call(arr) == "[object Array]";
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/type.js
var require_type = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./type')} */
	module.exports = TypeError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-object-atoms@1.1.2/node_modules/es-object-atoms/index.js
var require_es_object_atoms = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('.')} */
	module.exports = Object;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/index.js
var require_es_errors = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('.')} */
	module.exports = Error;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/eval.js
var require_eval = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./eval')} */
	module.exports = EvalError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/range.js
var require_range = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./range')} */
	module.exports = RangeError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/ref.js
var require_ref = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./ref')} */
	module.exports = ReferenceError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/syntax.js
var require_syntax = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./syntax')} */
	module.exports = SyntaxError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-errors@1.3.0/node_modules/es-errors/uri.js
var require_uri = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./uri')} */
	module.exports = URIError;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/abs.js
var require_abs = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./abs')} */
	module.exports = Math.abs;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/floor.js
var require_floor = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./floor')} */
	module.exports = Math.floor;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/max.js
var require_max = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./max')} */
	module.exports = Math.max;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/min.js
var require_min = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./min')} */
	module.exports = Math.min;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/pow.js
var require_pow = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./pow')} */
	module.exports = Math.pow;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/round.js
var require_round = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./round')} */
	module.exports = Math.round;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/isNaN.js
var require_isNaN = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./isNaN')} */
	module.exports = Number.isNaN || function isNaN(a) {
		return a !== a;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/math-intrinsics@1.1.0/node_modules/math-intrinsics/sign.js
var require_sign = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var $isNaN = require_isNaN();
	/** @type {import('./sign')} */
	module.exports = function sign(number) {
		if ($isNaN(number) || number === 0) return number;
		return number < 0 ? -1 : 1;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/gopd@1.2.0/node_modules/gopd/gOPD.js
var require_gOPD = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./gOPD')} */
	module.exports = Object.getOwnPropertyDescriptor;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/gopd@1.2.0/node_modules/gopd/index.js
var require_gopd = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('.')} */
	var $gOPD = require_gOPD();
	if ($gOPD) try {
		$gOPD([], "length");
	} catch (e) {
		$gOPD = null;
	}
	module.exports = $gOPD;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/es-define-property@1.0.1/node_modules/es-define-property/index.js
var require_es_define_property = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('.')} */
	var $defineProperty = Object.defineProperty || false;
	if ($defineProperty) try {
		$defineProperty({}, "a", { value: 1 });
	} catch (e) {
		$defineProperty = false;
	}
	module.exports = $defineProperty;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/has-symbols@1.1.0/node_modules/has-symbols/shams.js
var require_shams$1 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./shams')} */
	module.exports = function hasSymbols() {
		if (typeof Symbol !== "function" || typeof Object.getOwnPropertySymbols !== "function") return false;
		if (typeof Symbol.iterator === "symbol") return true;
		/** @type {{ [k in symbol]?: unknown }} */
		var obj = {};
		var sym = Symbol("test");
		var symObj = Object(sym);
		if (typeof sym === "string") return false;
		if (Object.prototype.toString.call(sym) !== "[object Symbol]") return false;
		if (Object.prototype.toString.call(symObj) !== "[object Symbol]") return false;
		var symVal = 42;
		obj[sym] = symVal;
		for (var _ in obj) return false;
		if (typeof Object.keys === "function" && Object.keys(obj).length !== 0) return false;
		if (typeof Object.getOwnPropertyNames === "function" && Object.getOwnPropertyNames(obj).length !== 0) return false;
		var syms = Object.getOwnPropertySymbols(obj);
		if (syms.length !== 1 || syms[0] !== sym) return false;
		if (!Object.prototype.propertyIsEnumerable.call(obj, sym)) return false;
		if (typeof Object.getOwnPropertyDescriptor === "function") {
			var descriptor = Object.getOwnPropertyDescriptor(obj, sym);
			if (descriptor.value !== symVal || descriptor.enumerable !== true) return false;
		}
		return true;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/has-symbols@1.1.0/node_modules/has-symbols/index.js
var require_has_symbols = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var origSymbol = typeof Symbol !== "undefined" && Symbol;
	var hasSymbolSham = require_shams$1();
	/** @type {import('.')} */
	module.exports = function hasNativeSymbols() {
		if (typeof origSymbol !== "function") return false;
		if (typeof Symbol !== "function") return false;
		if (typeof origSymbol("foo") !== "symbol") return false;
		if (typeof Symbol("bar") !== "symbol") return false;
		return hasSymbolSham();
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/get-proto@1.0.1/node_modules/get-proto/Reflect.getPrototypeOf.js
var require_Reflect_getPrototypeOf = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./Reflect.getPrototypeOf')} */
	module.exports = typeof Reflect !== "undefined" && Reflect.getPrototypeOf || null;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/get-proto@1.0.1/node_modules/get-proto/Object.getPrototypeOf.js
var require_Object_getPrototypeOf = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./Object.getPrototypeOf')} */
	module.exports = require_es_object_atoms().getPrototypeOf || null;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/function-bind@1.1.2/node_modules/function-bind/implementation.js
var require_implementation = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var ERROR_MESSAGE = "Function.prototype.bind called on incompatible ";
	var toStr = Object.prototype.toString;
	var max = Math.max;
	var funcType = "[object Function]";
	var concatty = function concatty(a, b) {
		var arr = [];
		for (var i = 0; i < a.length; i += 1) arr[i] = a[i];
		for (var j = 0; j < b.length; j += 1) arr[j + a.length] = b[j];
		return arr;
	};
	var slicy = function slicy(arrLike, offset) {
		var arr = [];
		for (var i = offset || 0, j = 0; i < arrLike.length; i += 1, j += 1) arr[j] = arrLike[i];
		return arr;
	};
	var joiny = function(arr, joiner) {
		var str = "";
		for (var i = 0; i < arr.length; i += 1) {
			str += arr[i];
			if (i + 1 < arr.length) str += joiner;
		}
		return str;
	};
	module.exports = function bind(that) {
		var target = this;
		if (typeof target !== "function" || toStr.apply(target) !== funcType) throw new TypeError(ERROR_MESSAGE + target);
		var args = slicy(arguments, 1);
		var bound;
		var binder = function() {
			if (this instanceof bound) {
				var result = target.apply(this, concatty(args, arguments));
				if (Object(result) === result) return result;
				return this;
			}
			return target.apply(that, concatty(args, arguments));
		};
		var boundLength = max(0, target.length - args.length);
		var boundArgs = [];
		for (var i = 0; i < boundLength; i++) boundArgs[i] = "$" + i;
		bound = Function("binder", "return function (" + joiny(boundArgs, ",") + "){ return binder.apply(this,arguments); }")(binder);
		if (target.prototype) {
			var Empty = function Empty() {};
			Empty.prototype = target.prototype;
			bound.prototype = new Empty();
			Empty.prototype = null;
		}
		return bound;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/function-bind@1.1.2/node_modules/function-bind/index.js
var require_function_bind = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var implementation = require_implementation();
	module.exports = Function.prototype.bind || implementation;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/functionCall.js
var require_functionCall = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./functionCall')} */
	module.exports = Function.prototype.call;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/functionApply.js
var require_functionApply = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./functionApply')} */
	module.exports = Function.prototype.apply;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/reflectApply.js
var require_reflectApply = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('./reflectApply')} */
	module.exports = typeof Reflect !== "undefined" && Reflect && Reflect.apply;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/actualApply.js
var require_actualApply = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var bind = require_function_bind();
	var $apply = require_functionApply();
	var $call = require_functionCall();
	/** @type {import('./actualApply')} */
	module.exports = require_reflectApply() || bind.call($call, $apply);
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/index.js
var require_call_bind_apply_helpers = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var bind = require_function_bind();
	var $TypeError = require_type();
	var $call = require_functionCall();
	var $actualApply = require_actualApply();
	/** @type {(args: [Function, thisArg?: unknown, ...args: unknown[]]) => Function} TODO FIXME, find a way to use import('.') */
	module.exports = function callBindBasic(args) {
		if (args.length < 1 || typeof args[0] !== "function") throw new $TypeError("a function is required");
		return $actualApply(bind, $call, args);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/dunder-proto@1.0.1/node_modules/dunder-proto/get.js
var require_get = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var callBind = require_call_bind_apply_helpers();
	var gOPD = require_gopd();
	var hasProtoAccessor;
	try {
		hasProtoAccessor = [].__proto__ === Array.prototype;
	} catch (e) {
		if (!e || typeof e !== "object" || !("code" in e) || e.code !== "ERR_PROTO_ACCESS") throw e;
	}
	var desc = !!hasProtoAccessor && gOPD && gOPD(Object.prototype, "__proto__");
	var $Object = Object;
	var $getPrototypeOf = $Object.getPrototypeOf;
	/** @type {import('./get')} */
	module.exports = desc && typeof desc.get === "function" ? callBind([desc.get]) : typeof $getPrototypeOf === "function" ? function getDunder(value) {
		return $getPrototypeOf(value == null ? value : $Object(value));
	} : false;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/get-proto@1.0.1/node_modules/get-proto/index.js
var require_get_proto = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var reflectGetProto = require_Reflect_getPrototypeOf();
	var originalGetProto = require_Object_getPrototypeOf();
	var getDunderProto = require_get();
	/** @type {import('.')} */
	module.exports = reflectGetProto ? function getProto(O) {
		return reflectGetProto(O);
	} : originalGetProto ? function getProto(O) {
		if (!O || typeof O !== "object" && typeof O !== "function") throw new TypeError("getProto: not an object");
		return originalGetProto(O);
	} : getDunderProto ? function getProto(O) {
		return getDunderProto(O);
	} : null;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/hasown@2.0.4/node_modules/hasown/index.js
var require_hasown = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var call = Function.prototype.call;
	var $hasOwn = Object.prototype.hasOwnProperty;
	/** @type {import('.')} */
	module.exports = require_function_bind().call(call, $hasOwn);
}));
//#endregion
//#region ../../../../node_modules/.pnpm/get-intrinsic@1.3.0/node_modules/get-intrinsic/index.js
var require_get_intrinsic = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var undefined;
	var $Object = require_es_object_atoms();
	var $Error = require_es_errors();
	var $EvalError = require_eval();
	var $RangeError = require_range();
	var $ReferenceError = require_ref();
	var $SyntaxError = require_syntax();
	var $TypeError = require_type();
	var $URIError = require_uri();
	var abs = require_abs();
	var floor = require_floor();
	var max = require_max();
	var min = require_min();
	var pow = require_pow();
	var round = require_round();
	var sign = require_sign();
	var $Function = Function;
	var getEvalledConstructor = function(expressionSyntax) {
		try {
			return $Function("\"use strict\"; return (" + expressionSyntax + ").constructor;")();
		} catch (e) {}
	};
	var $gOPD = require_gopd();
	var $defineProperty = require_es_define_property();
	var throwTypeError = function() {
		throw new $TypeError();
	};
	var ThrowTypeError = $gOPD ? function() {
		try {
			arguments.callee;
			return throwTypeError;
		} catch (calleeThrows) {
			try {
				return $gOPD(arguments, "callee").get;
			} catch (gOPDthrows) {
				return throwTypeError;
			}
		}
	}() : throwTypeError;
	var hasSymbols = require_has_symbols()();
	var getProto = require_get_proto();
	var $ObjectGPO = require_Object_getPrototypeOf();
	var $ReflectGPO = require_Reflect_getPrototypeOf();
	var $apply = require_functionApply();
	var $call = require_functionCall();
	var needsEval = {};
	var TypedArray = typeof Uint8Array === "undefined" || !getProto ? undefined : getProto(Uint8Array);
	var INTRINSICS = {
		__proto__: null,
		"%AggregateError%": typeof AggregateError === "undefined" ? undefined : AggregateError,
		"%Array%": Array,
		"%ArrayBuffer%": typeof ArrayBuffer === "undefined" ? undefined : ArrayBuffer,
		"%ArrayIteratorPrototype%": hasSymbols && getProto ? getProto([][Symbol.iterator]()) : undefined,
		"%AsyncFromSyncIteratorPrototype%": undefined,
		"%AsyncFunction%": needsEval,
		"%AsyncGenerator%": needsEval,
		"%AsyncGeneratorFunction%": needsEval,
		"%AsyncIteratorPrototype%": needsEval,
		"%Atomics%": typeof Atomics === "undefined" ? undefined : Atomics,
		"%BigInt%": typeof BigInt === "undefined" ? undefined : BigInt,
		"%BigInt64Array%": typeof BigInt64Array === "undefined" ? undefined : BigInt64Array,
		"%BigUint64Array%": typeof BigUint64Array === "undefined" ? undefined : BigUint64Array,
		"%Boolean%": Boolean,
		"%DataView%": typeof DataView === "undefined" ? undefined : DataView,
		"%Date%": Date,
		"%decodeURI%": decodeURI,
		"%decodeURIComponent%": decodeURIComponent,
		"%encodeURI%": encodeURI,
		"%encodeURIComponent%": encodeURIComponent,
		"%Error%": $Error,
		"%eval%": eval,
		"%EvalError%": $EvalError,
		"%Float16Array%": typeof Float16Array === "undefined" ? undefined : Float16Array,
		"%Float32Array%": typeof Float32Array === "undefined" ? undefined : Float32Array,
		"%Float64Array%": typeof Float64Array === "undefined" ? undefined : Float64Array,
		"%FinalizationRegistry%": typeof FinalizationRegistry === "undefined" ? undefined : FinalizationRegistry,
		"%Function%": $Function,
		"%GeneratorFunction%": needsEval,
		"%Int8Array%": typeof Int8Array === "undefined" ? undefined : Int8Array,
		"%Int16Array%": typeof Int16Array === "undefined" ? undefined : Int16Array,
		"%Int32Array%": typeof Int32Array === "undefined" ? undefined : Int32Array,
		"%isFinite%": isFinite,
		"%isNaN%": isNaN,
		"%IteratorPrototype%": hasSymbols && getProto ? getProto(getProto([][Symbol.iterator]())) : undefined,
		"%JSON%": typeof JSON === "object" ? JSON : undefined,
		"%Map%": typeof Map === "undefined" ? undefined : Map,
		"%MapIteratorPrototype%": typeof Map === "undefined" || !hasSymbols || !getProto ? undefined : getProto((/* @__PURE__ */ new Map())[Symbol.iterator]()),
		"%Math%": Math,
		"%Number%": Number,
		"%Object%": $Object,
		"%Object.getOwnPropertyDescriptor%": $gOPD,
		"%parseFloat%": parseFloat,
		"%parseInt%": parseInt,
		"%Promise%": typeof Promise === "undefined" ? undefined : Promise,
		"%Proxy%": typeof Proxy === "undefined" ? undefined : Proxy,
		"%RangeError%": $RangeError,
		"%ReferenceError%": $ReferenceError,
		"%Reflect%": typeof Reflect === "undefined" ? undefined : Reflect,
		"%RegExp%": RegExp,
		"%Set%": typeof Set === "undefined" ? undefined : Set,
		"%SetIteratorPrototype%": typeof Set === "undefined" || !hasSymbols || !getProto ? undefined : getProto((/* @__PURE__ */ new Set())[Symbol.iterator]()),
		"%SharedArrayBuffer%": typeof SharedArrayBuffer === "undefined" ? undefined : SharedArrayBuffer,
		"%String%": String,
		"%StringIteratorPrototype%": hasSymbols && getProto ? getProto(""[Symbol.iterator]()) : undefined,
		"%Symbol%": hasSymbols ? Symbol : undefined,
		"%SyntaxError%": $SyntaxError,
		"%ThrowTypeError%": ThrowTypeError,
		"%TypedArray%": TypedArray,
		"%TypeError%": $TypeError,
		"%Uint8Array%": typeof Uint8Array === "undefined" ? undefined : Uint8Array,
		"%Uint8ClampedArray%": typeof Uint8ClampedArray === "undefined" ? undefined : Uint8ClampedArray,
		"%Uint16Array%": typeof Uint16Array === "undefined" ? undefined : Uint16Array,
		"%Uint32Array%": typeof Uint32Array === "undefined" ? undefined : Uint32Array,
		"%URIError%": $URIError,
		"%WeakMap%": typeof WeakMap === "undefined" ? undefined : WeakMap,
		"%WeakRef%": typeof WeakRef === "undefined" ? undefined : WeakRef,
		"%WeakSet%": typeof WeakSet === "undefined" ? undefined : WeakSet,
		"%Function.prototype.call%": $call,
		"%Function.prototype.apply%": $apply,
		"%Object.defineProperty%": $defineProperty,
		"%Object.getPrototypeOf%": $ObjectGPO,
		"%Math.abs%": abs,
		"%Math.floor%": floor,
		"%Math.max%": max,
		"%Math.min%": min,
		"%Math.pow%": pow,
		"%Math.round%": round,
		"%Math.sign%": sign,
		"%Reflect.getPrototypeOf%": $ReflectGPO
	};
	if (getProto) try {
		null.error;
	} catch (e) {
		INTRINSICS["%Error.prototype%"] = getProto(getProto(e));
	}
	var doEval = function doEval(name) {
		var value;
		if (name === "%AsyncFunction%") value = getEvalledConstructor("async function () {}");
		else if (name === "%GeneratorFunction%") value = getEvalledConstructor("function* () {}");
		else if (name === "%AsyncGeneratorFunction%") value = getEvalledConstructor("async function* () {}");
		else if (name === "%AsyncGenerator%") {
			var fn = doEval("%AsyncGeneratorFunction%");
			if (fn) value = fn.prototype;
		} else if (name === "%AsyncIteratorPrototype%") {
			var gen = doEval("%AsyncGenerator%");
			if (gen && getProto) value = getProto(gen.prototype);
		}
		INTRINSICS[name] = value;
		return value;
	};
	var LEGACY_ALIASES = {
		__proto__: null,
		"%ArrayBufferPrototype%": ["ArrayBuffer", "prototype"],
		"%ArrayPrototype%": ["Array", "prototype"],
		"%ArrayProto_entries%": [
			"Array",
			"prototype",
			"entries"
		],
		"%ArrayProto_forEach%": [
			"Array",
			"prototype",
			"forEach"
		],
		"%ArrayProto_keys%": [
			"Array",
			"prototype",
			"keys"
		],
		"%ArrayProto_values%": [
			"Array",
			"prototype",
			"values"
		],
		"%AsyncFunctionPrototype%": ["AsyncFunction", "prototype"],
		"%AsyncGenerator%": ["AsyncGeneratorFunction", "prototype"],
		"%AsyncGeneratorPrototype%": [
			"AsyncGeneratorFunction",
			"prototype",
			"prototype"
		],
		"%BooleanPrototype%": ["Boolean", "prototype"],
		"%DataViewPrototype%": ["DataView", "prototype"],
		"%DatePrototype%": ["Date", "prototype"],
		"%ErrorPrototype%": ["Error", "prototype"],
		"%EvalErrorPrototype%": ["EvalError", "prototype"],
		"%Float32ArrayPrototype%": ["Float32Array", "prototype"],
		"%Float64ArrayPrototype%": ["Float64Array", "prototype"],
		"%FunctionPrototype%": ["Function", "prototype"],
		"%Generator%": ["GeneratorFunction", "prototype"],
		"%GeneratorPrototype%": [
			"GeneratorFunction",
			"prototype",
			"prototype"
		],
		"%Int8ArrayPrototype%": ["Int8Array", "prototype"],
		"%Int16ArrayPrototype%": ["Int16Array", "prototype"],
		"%Int32ArrayPrototype%": ["Int32Array", "prototype"],
		"%JSONParse%": ["JSON", "parse"],
		"%JSONStringify%": ["JSON", "stringify"],
		"%MapPrototype%": ["Map", "prototype"],
		"%NumberPrototype%": ["Number", "prototype"],
		"%ObjectPrototype%": ["Object", "prototype"],
		"%ObjProto_toString%": [
			"Object",
			"prototype",
			"toString"
		],
		"%ObjProto_valueOf%": [
			"Object",
			"prototype",
			"valueOf"
		],
		"%PromisePrototype%": ["Promise", "prototype"],
		"%PromiseProto_then%": [
			"Promise",
			"prototype",
			"then"
		],
		"%Promise_all%": ["Promise", "all"],
		"%Promise_reject%": ["Promise", "reject"],
		"%Promise_resolve%": ["Promise", "resolve"],
		"%RangeErrorPrototype%": ["RangeError", "prototype"],
		"%ReferenceErrorPrototype%": ["ReferenceError", "prototype"],
		"%RegExpPrototype%": ["RegExp", "prototype"],
		"%SetPrototype%": ["Set", "prototype"],
		"%SharedArrayBufferPrototype%": ["SharedArrayBuffer", "prototype"],
		"%StringPrototype%": ["String", "prototype"],
		"%SymbolPrototype%": ["Symbol", "prototype"],
		"%SyntaxErrorPrototype%": ["SyntaxError", "prototype"],
		"%TypedArrayPrototype%": ["TypedArray", "prototype"],
		"%TypeErrorPrototype%": ["TypeError", "prototype"],
		"%Uint8ArrayPrototype%": ["Uint8Array", "prototype"],
		"%Uint8ClampedArrayPrototype%": ["Uint8ClampedArray", "prototype"],
		"%Uint16ArrayPrototype%": ["Uint16Array", "prototype"],
		"%Uint32ArrayPrototype%": ["Uint32Array", "prototype"],
		"%URIErrorPrototype%": ["URIError", "prototype"],
		"%WeakMapPrototype%": ["WeakMap", "prototype"],
		"%WeakSetPrototype%": ["WeakSet", "prototype"]
	};
	var bind = require_function_bind();
	var hasOwn = require_hasown();
	var $concat = bind.call($call, Array.prototype.concat);
	var $spliceApply = bind.call($apply, Array.prototype.splice);
	var $replace = bind.call($call, String.prototype.replace);
	var $strSlice = bind.call($call, String.prototype.slice);
	var $exec = bind.call($call, RegExp.prototype.exec);
	var rePropName = /[^%.[\]]+|\[(?:(-?\d+(?:\.\d+)?)|(["'])((?:(?!\2)[^\\]|\\.)*?)\2)\]|(?=(?:\.|\[\])(?:\.|\[\]|%$))/g;
	var reEscapeChar = /\\(\\)?/g;
	var stringToPath = function stringToPath(string) {
		var first = $strSlice(string, 0, 1);
		var last = $strSlice(string, -1);
		if (first === "%" && last !== "%") throw new $SyntaxError("invalid intrinsic syntax, expected closing `%`");
		else if (last === "%" && first !== "%") throw new $SyntaxError("invalid intrinsic syntax, expected opening `%`");
		var result = [];
		$replace(string, rePropName, function(match, number, quote, subString) {
			result[result.length] = quote ? $replace(subString, reEscapeChar, "$1") : number || match;
		});
		return result;
	};
	var getBaseIntrinsic = function getBaseIntrinsic(name, allowMissing) {
		var intrinsicName = name;
		var alias;
		if (hasOwn(LEGACY_ALIASES, intrinsicName)) {
			alias = LEGACY_ALIASES[intrinsicName];
			intrinsicName = "%" + alias[0] + "%";
		}
		if (hasOwn(INTRINSICS, intrinsicName)) {
			var value = INTRINSICS[intrinsicName];
			if (value === needsEval) value = doEval(intrinsicName);
			if (typeof value === "undefined" && !allowMissing) throw new $TypeError("intrinsic " + name + " exists, but is not available. Please file an issue!");
			return {
				alias,
				name: intrinsicName,
				value
			};
		}
		throw new $SyntaxError("intrinsic " + name + " does not exist!");
	};
	module.exports = function GetIntrinsic(name, allowMissing) {
		if (typeof name !== "string" || name.length === 0) throw new $TypeError("intrinsic name must be a non-empty string");
		if (arguments.length > 1 && typeof allowMissing !== "boolean") throw new $TypeError("\"allowMissing\" argument must be a boolean");
		if ($exec(/^%?[^%]*%?$/, name) === null) throw new $SyntaxError("`%` may not be present anywhere but at the beginning and end of the intrinsic name");
		var parts = stringToPath(name);
		var intrinsicBaseName = parts.length > 0 ? parts[0] : "";
		var intrinsic = getBaseIntrinsic("%" + intrinsicBaseName + "%", allowMissing);
		var intrinsicRealName = intrinsic.name;
		var value = intrinsic.value;
		var skipFurtherCaching = false;
		var alias = intrinsic.alias;
		if (alias) {
			intrinsicBaseName = alias[0];
			$spliceApply(parts, $concat([0, 1], alias));
		}
		for (var i = 1, isOwn = true; i < parts.length; i += 1) {
			var part = parts[i];
			var first = $strSlice(part, 0, 1);
			var last = $strSlice(part, -1);
			if ((first === "\"" || first === "'" || first === "`" || last === "\"" || last === "'" || last === "`") && first !== last) throw new $SyntaxError("property names with quotes must have matching quotes");
			if (part === "constructor" || !isOwn) skipFurtherCaching = true;
			intrinsicBaseName += "." + part;
			intrinsicRealName = "%" + intrinsicBaseName + "%";
			if (hasOwn(INTRINSICS, intrinsicRealName)) value = INTRINSICS[intrinsicRealName];
			else if (value != null) {
				if (!(part in value)) {
					if (!allowMissing) throw new $TypeError("base intrinsic for " + name + " exists, but the property is not available.");
					return;
				}
				if ($gOPD && i + 1 >= parts.length) {
					var desc = $gOPD(value, part);
					isOwn = !!desc;
					if (isOwn && "get" in desc && !("originalValue" in desc.get)) value = desc.get;
					else value = value[part];
				} else {
					isOwn = hasOwn(value, part);
					value = value[part];
				}
				if (isOwn && !skipFurtherCaching) INTRINSICS[intrinsicRealName] = value;
			}
		}
		return value;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bound@1.0.4/node_modules/call-bound/index.js
var require_call_bound = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var GetIntrinsic = require_get_intrinsic();
	var callBindBasic = require_call_bind_apply_helpers();
	/** @type {(thisArg: string, searchString: string, position?: number) => number} */
	var $indexOf = callBindBasic([GetIntrinsic("%String.prototype.indexOf%")]);
	/** @type {import('.')} */
	module.exports = function callBoundIntrinsic(name, allowMissing) {
		var intrinsic = GetIntrinsic(name, !!allowMissing);
		if (typeof intrinsic === "function" && $indexOf(name, ".prototype.") > -1) return callBindBasic([intrinsic]);
		return intrinsic;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/is-callable@1.2.7/node_modules/is-callable/index.js
var require_is_callable = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var fnToStr = Function.prototype.toString;
	var reflectApply = typeof Reflect === "object" && Reflect !== null && Reflect.apply;
	var badArrayLike;
	var isCallableMarker;
	if (typeof reflectApply === "function" && typeof Object.defineProperty === "function") try {
		badArrayLike = Object.defineProperty({}, "length", { get: function() {
			throw isCallableMarker;
		} });
		isCallableMarker = {};
		reflectApply(function() {
			throw 42;
		}, null, badArrayLike);
	} catch (_) {
		if (_ !== isCallableMarker) reflectApply = null;
	}
	else reflectApply = null;
	var constructorRegex = /^\s*class\b/;
	var isES6ClassFn = function isES6ClassFunction(value) {
		try {
			var fnStr = fnToStr.call(value);
			return constructorRegex.test(fnStr);
		} catch (e) {
			return false;
		}
	};
	var tryFunctionObject = function tryFunctionToStr(value) {
		try {
			if (isES6ClassFn(value)) return false;
			fnToStr.call(value);
			return true;
		} catch (e) {
			return false;
		}
	};
	var toStr = Object.prototype.toString;
	var objectClass = "[object Object]";
	var fnClass = "[object Function]";
	var genClass = "[object GeneratorFunction]";
	var ddaClass = "[object HTMLAllCollection]";
	var ddaClass2 = "[object HTML document.all class]";
	var ddaClass3 = "[object HTMLCollection]";
	var hasToStringTag = typeof Symbol === "function" && !!Symbol.toStringTag;
	var isIE68 = !(0 in [,]);
	var isDDA = function isDocumentDotAll() {
		return false;
	};
	if (typeof document === "object") {
		var all = document.all;
		if (toStr.call(all) === toStr.call(document.all)) isDDA = function isDocumentDotAll(value) {
			if ((isIE68 || !value) && (typeof value === "undefined" || typeof value === "object")) try {
				var str = toStr.call(value);
				return (str === ddaClass || str === ddaClass2 || str === ddaClass3 || str === objectClass) && value("") == null;
			} catch (e) {}
			return false;
		};
	}
	module.exports = reflectApply ? function isCallable(value) {
		if (isDDA(value)) return true;
		if (!value) return false;
		if (typeof value !== "function" && typeof value !== "object") return false;
		try {
			reflectApply(value, null, badArrayLike);
		} catch (e) {
			if (e !== isCallableMarker) return false;
		}
		return !isES6ClassFn(value) && tryFunctionObject(value);
	} : function isCallable(value) {
		if (isDDA(value)) return true;
		if (!value) return false;
		if (typeof value !== "function" && typeof value !== "object") return false;
		if (hasToStringTag) return tryFunctionObject(value);
		if (isES6ClassFn(value)) return false;
		var strClass = toStr.call(value);
		if (strClass !== fnClass && strClass !== genClass && !/^\[object HTML/.test(strClass)) return false;
		return tryFunctionObject(value);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/for-each@0.3.5/node_modules/for-each/index.js
var require_for_each = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var isCallable = require_is_callable();
	var toStr = Object.prototype.toString;
	var hasOwnProperty = Object.prototype.hasOwnProperty;
	/** @type {<This, A extends readonly unknown[]>(arr: A, iterator: (this: This | void, value: A[number], index: number, arr: A) => void, receiver: This | undefined) => void} */
	var forEachArray = function forEachArray(array, iterator, receiver) {
		for (var i = 0, len = array.length; i < len; i++) if (hasOwnProperty.call(array, i)) if (receiver == null) iterator(array[i], i, array);
		else iterator.call(receiver, array[i], i, array);
	};
	/** @type {<This, S extends string>(string: S, iterator: (this: This | void, value: S[number], index: number, string: S) => void, receiver: This | undefined) => void} */
	var forEachString = function forEachString(string, iterator, receiver) {
		for (var i = 0, len = string.length; i < len; i++) if (receiver == null) iterator(string.charAt(i), i, string);
		else iterator.call(receiver, string.charAt(i), i, string);
	};
	/** @type {<This, O>(obj: O, iterator: (this: This | void, value: O[keyof O], index: keyof O, obj: O) => void, receiver: This | undefined) => void} */
	var forEachObject = function forEachObject(object, iterator, receiver) {
		for (var k in object) if (hasOwnProperty.call(object, k)) if (receiver == null) iterator(object[k], k, object);
		else iterator.call(receiver, object[k], k, object);
	};
	/** @type {(x: unknown) => x is readonly unknown[]} */
	function isArray(x) {
		return toStr.call(x) === "[object Array]";
	}
	/** @type {import('.')._internal} */
	module.exports = function forEach(list, iterator, thisArg) {
		if (!isCallable(iterator)) throw new TypeError("iterator must be a function");
		var receiver;
		if (arguments.length >= 3) receiver = thisArg;
		if (isArray(list)) forEachArray(list, iterator, receiver);
		else if (typeof list === "string") forEachString(list, iterator, receiver);
		else forEachObject(list, iterator, receiver);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/possible-typed-array-names@1.1.0/node_modules/possible-typed-array-names/index.js
var require_possible_typed_array_names = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/** @type {import('.')} */
	module.exports = [
		"Float16Array",
		"Float32Array",
		"Float64Array",
		"Int8Array",
		"Int16Array",
		"Int32Array",
		"Uint8Array",
		"Uint8ClampedArray",
		"Uint16Array",
		"Uint32Array",
		"BigInt64Array",
		"BigUint64Array"
	];
}));
//#endregion
//#region ../../../../node_modules/.pnpm/available-typed-arrays@1.0.7/node_modules/available-typed-arrays/index.js
var require_available_typed_arrays = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	init_dist();
	var possibleNames = require_possible_typed_array_names();
	var g = typeof globalThis === "undefined" ? global : globalThis;
	/** @type {import('.')} */
	module.exports = function availableTypedArrays() {
		var out = [];
		for (var i = 0; i < possibleNames.length; i++) if (typeof g[possibleNames[i]] === "function") out[out.length] = possibleNames[i];
		return out;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/define-data-property@1.1.4/node_modules/define-data-property/index.js
var require_define_data_property = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var $defineProperty = require_es_define_property();
	var $SyntaxError = require_syntax();
	var $TypeError = require_type();
	var gopd = require_gopd();
	/** @type {import('.')} */
	module.exports = function defineDataProperty(obj, property, value) {
		if (!obj || typeof obj !== "object" && typeof obj !== "function") throw new $TypeError("`obj` must be an object or a function`");
		if (typeof property !== "string" && typeof property !== "symbol") throw new $TypeError("`property` must be a string or a symbol`");
		if (arguments.length > 3 && typeof arguments[3] !== "boolean" && arguments[3] !== null) throw new $TypeError("`nonEnumerable`, if provided, must be a boolean or null");
		if (arguments.length > 4 && typeof arguments[4] !== "boolean" && arguments[4] !== null) throw new $TypeError("`nonWritable`, if provided, must be a boolean or null");
		if (arguments.length > 5 && typeof arguments[5] !== "boolean" && arguments[5] !== null) throw new $TypeError("`nonConfigurable`, if provided, must be a boolean or null");
		if (arguments.length > 6 && typeof arguments[6] !== "boolean") throw new $TypeError("`loose`, if provided, must be a boolean");
		var nonEnumerable = arguments.length > 3 ? arguments[3] : null;
		var nonWritable = arguments.length > 4 ? arguments[4] : null;
		var nonConfigurable = arguments.length > 5 ? arguments[5] : null;
		var loose = arguments.length > 6 ? arguments[6] : false;
		var desc = !!gopd && gopd(obj, property);
		if ($defineProperty) $defineProperty(obj, property, {
			configurable: nonConfigurable === null && desc ? desc.configurable : !nonConfigurable,
			enumerable: nonEnumerable === null && desc ? desc.enumerable : !nonEnumerable,
			value,
			writable: nonWritable === null && desc ? desc.writable : !nonWritable
		});
		else if (loose || !nonEnumerable && !nonWritable && !nonConfigurable) obj[property] = value;
		else throw new $SyntaxError("This environment does not support defining a property as non-configurable, non-writable, or non-enumerable.");
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/has-property-descriptors@1.0.2/node_modules/has-property-descriptors/index.js
var require_has_property_descriptors = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var $defineProperty = require_es_define_property();
	var hasPropertyDescriptors = function hasPropertyDescriptors() {
		return !!$defineProperty;
	};
	hasPropertyDescriptors.hasArrayLengthDefineBug = function hasArrayLengthDefineBug() {
		if (!$defineProperty) return null;
		try {
			return $defineProperty([], "length", { value: 1 }).length !== 1;
		} catch (e) {
			return true;
		}
	};
	module.exports = hasPropertyDescriptors;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/set-function-length@1.2.2/node_modules/set-function-length/index.js
var require_set_function_length = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var GetIntrinsic = require_get_intrinsic();
	var define = require_define_data_property();
	var hasDescriptors = require_has_property_descriptors()();
	var gOPD = require_gopd();
	var $TypeError = require_type();
	var $floor = GetIntrinsic("%Math.floor%");
	/** @type {import('.')} */
	module.exports = function setFunctionLength(fn, length) {
		if (typeof fn !== "function") throw new $TypeError("`fn` is not a function");
		if (typeof length !== "number" || length < 0 || length > 4294967295 || $floor(length) !== length) throw new $TypeError("`length` must be a positive 32-bit integer");
		var loose = arguments.length > 2 && !!arguments[2];
		var functionLengthIsConfigurable = true;
		var functionLengthIsWritable = true;
		if ("length" in fn && gOPD) {
			var desc = gOPD(fn, "length");
			if (desc && !desc.configurable) functionLengthIsConfigurable = false;
			if (desc && !desc.writable) functionLengthIsWritable = false;
		}
		if (functionLengthIsConfigurable || functionLengthIsWritable || !loose) if (hasDescriptors) define(fn, "length", length, true, true);
		else define(fn, "length", length);
		return fn;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind-apply-helpers@1.0.2/node_modules/call-bind-apply-helpers/applyBind.js
var require_applyBind = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var bind = require_function_bind();
	var $apply = require_functionApply();
	var actualApply = require_actualApply();
	/** @type {import('./applyBind')} */
	module.exports = function applyBind() {
		return actualApply(bind, $apply, arguments);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/call-bind@1.0.9/node_modules/call-bind/index.js
var require_call_bind = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var setFunctionLength = require_set_function_length();
	var $defineProperty = require_es_define_property();
	var callBindBasic = require_call_bind_apply_helpers();
	var applyBind = require_applyBind();
	module.exports = function callBind(originalFunction) {
		var func = callBindBasic(arguments);
		var adjustedLength = 1 + originalFunction.length - (arguments.length - 1);
		return setFunctionLength(func, adjustedLength > 0 ? adjustedLength : 0, true);
	};
	if ($defineProperty) $defineProperty(module.exports, "apply", { value: applyBind });
	else module.exports.apply = applyBind;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/has-tostringtag@1.0.2/node_modules/has-tostringtag/shams.js
var require_shams = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var hasSymbols = require_shams$1();
	/** @type {import('.')} */
	module.exports = function hasToStringTagShams() {
		return hasSymbols() && !!Symbol.toStringTag;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/which-typed-array@1.1.22/node_modules/which-typed-array/index.js
var require_which_typed_array = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	init_dist();
	var forEach = require_for_each();
	var availableTypedArrays = require_available_typed_arrays();
	var callBind = require_call_bind();
	var callBound = require_call_bound();
	var gOPD = require_gopd();
	var getProto = require_get_proto();
	var $toString = callBound("Object.prototype.toString");
	var hasToStringTag = require_shams()();
	var g = typeof globalThis === "undefined" ? global : globalThis;
	var typedArrays = availableTypedArrays();
	var $slice = callBound("String.prototype.slice");
	/** @import { BoundSet, BoundSlice, Cache, Getter } from './types' */
	/** @import { TypedArrayName } from '.' */
	/** @type {<T = unknown>(array: readonly T[], value: unknown) => number} */
	var $indexOf = callBound("Array.prototype.indexOf", true) || function indexOf(array, value) {
		for (var i = 0; i < array.length; i += 1) if (array[i] === value) return i;
		return -1;
	};
	/** @type {Cache} */
	var cache = { __proto__: null };
	if (hasToStringTag && gOPD && getProto) forEach(typedArrays, function(typedArray) {
		var arr = new g[typedArray]();
		if (Symbol.toStringTag in arr && getProto) {
			var proto = getProto(arr);
			var descriptor = gOPD(proto, Symbol.toStringTag);
			if (!descriptor && proto) descriptor = gOPD(getProto(proto), Symbol.toStringTag);
			if (descriptor && descriptor.get) {
				var bound = callBind(descriptor.get);
				cache["$" + typedArray] = bound;
			}
		}
	});
	else forEach(typedArrays, function(typedArray) {
		var arr = new g[typedArray]();
		var fn = arr.slice || arr.set;
		if (fn) {
			var bound = callBind(fn);
			cache["$" + typedArray] = bound;
		}
	});
	/** @type {(value: object) => false | TypedArrayName} */
	function tryTypedArrays(value) {
		/** @type {ReturnType<typeof tryTypedArrays>} */ var found = false;
		forEach(
			cache,
			/** @param {Getter} getter @param {`$${TypedArrayName}`} typedArray */
			function(getter, typedArray) {
				if (!found) try {
					if ("$" + getter(value) === typedArray) found = $slice(typedArray, 1);
				} catch (e) {}
			}
		);
		return found;
	}
	/** @type {(value: object) => false | TypedArrayName} */
	function trySlices(value) {
		/** @type {ReturnType<typeof trySlices>} */ var found = false;
		forEach(
			cache,
			/** @param {Getter} getter @param {`$${TypedArrayName}`} name */
			function(getter, name) {
				if (!found) try {
					getter(value);
					found = $slice(name, 1);
				} catch (e) {}
			}
		);
		return found;
	}
	/** @type {(tag: unknown) => tag is typeof typedArrays[number]} */
	function isTATag(tag) {
		return $indexOf(typedArrays, tag) > -1;
	}
	/**
	* @type {import('.')}
	* @param {unknown} value
	*/
	module.exports = function whichTypedArray(value) {
		if (!value || typeof value !== "object") return false;
		if (!hasToStringTag) {
			var tag = $slice($toString(value), 8, -1);
			if (isTATag(tag)) return tag;
			if (tag !== "Object") return false;
			return trySlices(value);
		}
		if (!gOPD) return null;
		return tryTypedArrays(value);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/is-typed-array@1.1.15/node_modules/is-typed-array/index.js
var require_is_typed_array = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var whichTypedArray = require_which_typed_array();
	/** @type {import('.')} */
	module.exports = function isTypedArray(value) {
		return !!whichTypedArray(value);
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/typed-array-buffer@1.0.3/node_modules/typed-array-buffer/index.js
var require_typed_array_buffer = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var $TypeError = require_type();
	/** @type {undefined | ((thisArg: import('.').TypedArray) => Buffer<ArrayBufferLike>)} */
	var $typedArrayBuffer = require_call_bound()("TypedArray.prototype.buffer", true);
	var isTypedArray = require_is_typed_array();
	/** @type {import('.')} */
	module.exports = $typedArrayBuffer || function typedArrayBuffer(x) {
		if (!isTypedArray(x)) throw new $TypeError("Not a Typed Array");
		return x.buffer;
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/to-buffer@1.2.2/node_modules/to-buffer/index.js
var require_to_buffer = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var Buffer = require_safe_buffer().Buffer;
	var isArray = require_isarray();
	var typedArrayBuffer = require_typed_array_buffer();
	var isView = ArrayBuffer.isView || function isView(obj) {
		try {
			typedArrayBuffer(obj);
			return true;
		} catch (e) {
			return false;
		}
	};
	var useUint8Array = typeof Uint8Array !== "undefined";
	var useArrayBuffer = typeof ArrayBuffer !== "undefined" && typeof Uint8Array !== "undefined";
	var useFromArrayBuffer = useArrayBuffer && (Buffer.prototype instanceof Uint8Array || Buffer.TYPED_ARRAY_SUPPORT);
	module.exports = function toBuffer(data, encoding) {
		if (Buffer.isBuffer(data)) {
			if (data.constructor && !("isBuffer" in data)) return Buffer.from(data);
			return data;
		}
		if (typeof data === "string") return Buffer.from(data, encoding);
		if (useArrayBuffer && isView(data)) {
			if (data.byteLength === 0) return Buffer.alloc(0);
			if (useFromArrayBuffer) {
				var res = Buffer.from(data.buffer, data.byteOffset, data.byteLength);
				if (res.byteLength === data.byteLength) return res;
			}
			var uint8 = data instanceof Uint8Array ? data : new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
			var result = Buffer.from(uint8);
			if (result.length === data.byteLength) return result;
		}
		if (useUint8Array && data instanceof Uint8Array) return Buffer.from(data);
		var isArr = isArray(data);
		if (isArr) for (var i = 0; i < data.length; i += 1) {
			var x = data[i];
			if (typeof x !== "number" || x < 0 || x > 255 || ~~x !== x) throw new RangeError("Array items must be numbers in the range 0-255.");
		}
		if (isArr || Buffer.isBuffer(data) && data.constructor && typeof data.constructor.isBuffer === "function" && data.constructor.isBuffer(data)) return Buffer.from(data);
		throw new TypeError("The \"data\" argument must be a string, an Array, a Buffer, a Uint8Array, or a DataView.");
	};
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/hash.js
var require_hash = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var Buffer = require_safe_buffer().Buffer;
	var toBuffer = require_to_buffer();
	function Hash(blockSize, finalSize) {
		this._block = Buffer.alloc(blockSize);
		this._finalSize = finalSize;
		this._blockSize = blockSize;
		this._len = 0;
	}
	Hash.prototype.update = function(data, enc) {
		data = toBuffer(data, enc || "utf8");
		var block = this._block;
		var blockSize = this._blockSize;
		var length = data.length;
		var accum = this._len;
		for (var offset = 0; offset < length;) {
			var assigned = accum % blockSize;
			var remainder = Math.min(length - offset, blockSize - assigned);
			for (var i = 0; i < remainder; i++) block[assigned + i] = data[offset + i];
			accum += remainder;
			offset += remainder;
			if (accum % blockSize === 0) this._update(block);
		}
		this._len += length;
		return this;
	};
	Hash.prototype.digest = function(enc) {
		var rem = this._len % this._blockSize;
		this._block[rem] = 128;
		this._block.fill(0, rem + 1);
		if (rem >= this._finalSize) {
			this._update(this._block);
			this._block.fill(0);
		}
		var bits = this._len * 8;
		if (bits <= 4294967295) this._block.writeUInt32BE(bits, this._blockSize - 4);
		else {
			var lowBits = (bits & 4294967295) >>> 0;
			var highBits = (bits - lowBits) / 4294967296;
			this._block.writeUInt32BE(highBits, this._blockSize - 8);
			this._block.writeUInt32BE(lowBits, this._blockSize - 4);
		}
		this._update(this._block);
		var hash = this._hash();
		return enc ? hash.toString(enc) : hash;
	};
	Hash.prototype._update = function() {
		throw new Error("_update must be implemented by subclass");
	};
	module.exports = Hash;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha.js
var require_sha = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var inherits = require_inherits_browser();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var K = [
		1518500249,
		1859775393,
		-1894007588,
		-899497514
	];
	var W = new Array(80);
	function Sha() {
		this.init();
		this._w = W;
		Hash.call(this, 64, 56);
	}
	inherits(Sha, Hash);
	Sha.prototype.init = function() {
		this._a = 1732584193;
		this._b = 4023233417;
		this._c = 2562383102;
		this._d = 271733878;
		this._e = 3285377520;
		return this;
	};
	function rotl5(num) {
		return num << 5 | num >>> 27;
	}
	function rotl30(num) {
		return num << 30 | num >>> 2;
	}
	function ft(s, b, c, d) {
		if (s === 0) return b & c | ~b & d;
		if (s === 2) return b & c | b & d | c & d;
		return b ^ c ^ d;
	}
	Sha.prototype._update = function(M) {
		var w = this._w;
		var a = this._a | 0;
		var b = this._b | 0;
		var c = this._c | 0;
		var d = this._d | 0;
		var e = this._e | 0;
		for (var i = 0; i < 16; ++i) w[i] = M.readInt32BE(i * 4);
		for (; i < 80; ++i) w[i] = w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16];
		for (var j = 0; j < 80; ++j) {
			var s = ~~(j / 20);
			var t = rotl5(a) + ft(s, b, c, d) + e + w[j] + K[s] | 0;
			e = d;
			d = c;
			c = rotl30(b);
			b = a;
			a = t;
		}
		this._a = a + this._a | 0;
		this._b = b + this._b | 0;
		this._c = c + this._c | 0;
		this._d = d + this._d | 0;
		this._e = e + this._e | 0;
	};
	Sha.prototype._hash = function() {
		var H = Buffer.allocUnsafe(20);
		H.writeInt32BE(this._a | 0, 0);
		H.writeInt32BE(this._b | 0, 4);
		H.writeInt32BE(this._c | 0, 8);
		H.writeInt32BE(this._d | 0, 12);
		H.writeInt32BE(this._e | 0, 16);
		return H;
	};
	module.exports = Sha;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha1.js
var require_sha1 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var inherits = require_inherits_browser();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var K = [
		1518500249,
		1859775393,
		-1894007588,
		-899497514
	];
	var W = new Array(80);
	function Sha1() {
		this.init();
		this._w = W;
		Hash.call(this, 64, 56);
	}
	inherits(Sha1, Hash);
	Sha1.prototype.init = function() {
		this._a = 1732584193;
		this._b = 4023233417;
		this._c = 2562383102;
		this._d = 271733878;
		this._e = 3285377520;
		return this;
	};
	function rotl1(num) {
		return num << 1 | num >>> 31;
	}
	function rotl5(num) {
		return num << 5 | num >>> 27;
	}
	function rotl30(num) {
		return num << 30 | num >>> 2;
	}
	function ft(s, b, c, d) {
		if (s === 0) return b & c | ~b & d;
		if (s === 2) return b & c | b & d | c & d;
		return b ^ c ^ d;
	}
	Sha1.prototype._update = function(M) {
		var w = this._w;
		var a = this._a | 0;
		var b = this._b | 0;
		var c = this._c | 0;
		var d = this._d | 0;
		var e = this._e | 0;
		for (var i = 0; i < 16; ++i) w[i] = M.readInt32BE(i * 4);
		for (; i < 80; ++i) w[i] = rotl1(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16]);
		for (var j = 0; j < 80; ++j) {
			var s = ~~(j / 20);
			var t = rotl5(a) + ft(s, b, c, d) + e + w[j] + K[s] | 0;
			e = d;
			d = c;
			c = rotl30(b);
			b = a;
			a = t;
		}
		this._a = a + this._a | 0;
		this._b = b + this._b | 0;
		this._c = c + this._c | 0;
		this._d = d + this._d | 0;
		this._e = e + this._e | 0;
	};
	Sha1.prototype._hash = function() {
		var H = Buffer.allocUnsafe(20);
		H.writeInt32BE(this._a | 0, 0);
		H.writeInt32BE(this._b | 0, 4);
		H.writeInt32BE(this._c | 0, 8);
		H.writeInt32BE(this._d | 0, 12);
		H.writeInt32BE(this._e | 0, 16);
		return H;
	};
	module.exports = Sha1;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha256.js
var require_sha256 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/**
	* A JavaScript implementation of the Secure Hash Algorithm, SHA-256, as defined
	* in FIPS 180-2
	* Version 2.2-beta Copyright Angel Marin, Paul Johnston 2000 - 2009.
	* Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
	*
	*/
	var inherits = require_inherits_browser();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var K = [
		1116352408,
		1899447441,
		3049323471,
		3921009573,
		961987163,
		1508970993,
		2453635748,
		2870763221,
		3624381080,
		310598401,
		607225278,
		1426881987,
		1925078388,
		2162078206,
		2614888103,
		3248222580,
		3835390401,
		4022224774,
		264347078,
		604807628,
		770255983,
		1249150122,
		1555081692,
		1996064986,
		2554220882,
		2821834349,
		2952996808,
		3210313671,
		3336571891,
		3584528711,
		113926993,
		338241895,
		666307205,
		773529912,
		1294757372,
		1396182291,
		1695183700,
		1986661051,
		2177026350,
		2456956037,
		2730485921,
		2820302411,
		3259730800,
		3345764771,
		3516065817,
		3600352804,
		4094571909,
		275423344,
		430227734,
		506948616,
		659060556,
		883997877,
		958139571,
		1322822218,
		1537002063,
		1747873779,
		1955562222,
		2024104815,
		2227730452,
		2361852424,
		2428436474,
		2756734187,
		3204031479,
		3329325298
	];
	var W = new Array(64);
	function Sha256() {
		this.init();
		this._w = W;
		Hash.call(this, 64, 56);
	}
	inherits(Sha256, Hash);
	Sha256.prototype.init = function() {
		this._a = 1779033703;
		this._b = 3144134277;
		this._c = 1013904242;
		this._d = 2773480762;
		this._e = 1359893119;
		this._f = 2600822924;
		this._g = 528734635;
		this._h = 1541459225;
		return this;
	};
	function ch(x, y, z) {
		return z ^ x & (y ^ z);
	}
	function maj(x, y, z) {
		return x & y | z & (x | y);
	}
	function sigma0(x) {
		return (x >>> 2 | x << 30) ^ (x >>> 13 | x << 19) ^ (x >>> 22 | x << 10);
	}
	function sigma1(x) {
		return (x >>> 6 | x << 26) ^ (x >>> 11 | x << 21) ^ (x >>> 25 | x << 7);
	}
	function gamma0(x) {
		return (x >>> 7 | x << 25) ^ (x >>> 18 | x << 14) ^ x >>> 3;
	}
	function gamma1(x) {
		return (x >>> 17 | x << 15) ^ (x >>> 19 | x << 13) ^ x >>> 10;
	}
	Sha256.prototype._update = function(M) {
		var w = this._w;
		var a = this._a | 0;
		var b = this._b | 0;
		var c = this._c | 0;
		var d = this._d | 0;
		var e = this._e | 0;
		var f = this._f | 0;
		var g = this._g | 0;
		var h = this._h | 0;
		for (var i = 0; i < 16; ++i) w[i] = M.readInt32BE(i * 4);
		for (; i < 64; ++i) w[i] = gamma1(w[i - 2]) + w[i - 7] + gamma0(w[i - 15]) + w[i - 16] | 0;
		for (var j = 0; j < 64; ++j) {
			var T1 = h + sigma1(e) + ch(e, f, g) + K[j] + w[j] | 0;
			var T2 = sigma0(a) + maj(a, b, c) | 0;
			h = g;
			g = f;
			f = e;
			e = d + T1 | 0;
			d = c;
			c = b;
			b = a;
			a = T1 + T2 | 0;
		}
		this._a = a + this._a | 0;
		this._b = b + this._b | 0;
		this._c = c + this._c | 0;
		this._d = d + this._d | 0;
		this._e = e + this._e | 0;
		this._f = f + this._f | 0;
		this._g = g + this._g | 0;
		this._h = h + this._h | 0;
	};
	Sha256.prototype._hash = function() {
		var H = Buffer.allocUnsafe(32);
		H.writeInt32BE(this._a, 0);
		H.writeInt32BE(this._b, 4);
		H.writeInt32BE(this._c, 8);
		H.writeInt32BE(this._d, 12);
		H.writeInt32BE(this._e, 16);
		H.writeInt32BE(this._f, 20);
		H.writeInt32BE(this._g, 24);
		H.writeInt32BE(this._h, 28);
		return H;
	};
	module.exports = Sha256;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha224.js
var require_sha224 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	/**
	* A JavaScript implementation of the Secure Hash Algorithm, SHA-256, as defined
	* in FIPS 180-2
	* Version 2.2-beta Copyright Angel Marin, Paul Johnston 2000 - 2009.
	* Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
	*
	*/
	var inherits = require_inherits_browser();
	var Sha256 = require_sha256();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var W = new Array(64);
	function Sha224() {
		this.init();
		this._w = W;
		Hash.call(this, 64, 56);
	}
	inherits(Sha224, Sha256);
	Sha224.prototype.init = function() {
		this._a = 3238371032;
		this._b = 914150663;
		this._c = 812702999;
		this._d = 4144912697;
		this._e = 4290775857;
		this._f = 1750603025;
		this._g = 1694076839;
		this._h = 3204075428;
		return this;
	};
	Sha224.prototype._hash = function() {
		var H = Buffer.allocUnsafe(28);
		H.writeInt32BE(this._a, 0);
		H.writeInt32BE(this._b, 4);
		H.writeInt32BE(this._c, 8);
		H.writeInt32BE(this._d, 12);
		H.writeInt32BE(this._e, 16);
		H.writeInt32BE(this._f, 20);
		H.writeInt32BE(this._g, 24);
		return H;
	};
	module.exports = Sha224;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha512.js
var require_sha512 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var inherits = require_inherits_browser();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var K = [
		1116352408,
		3609767458,
		1899447441,
		602891725,
		3049323471,
		3964484399,
		3921009573,
		2173295548,
		961987163,
		4081628472,
		1508970993,
		3053834265,
		2453635748,
		2937671579,
		2870763221,
		3664609560,
		3624381080,
		2734883394,
		310598401,
		1164996542,
		607225278,
		1323610764,
		1426881987,
		3590304994,
		1925078388,
		4068182383,
		2162078206,
		991336113,
		2614888103,
		633803317,
		3248222580,
		3479774868,
		3835390401,
		2666613458,
		4022224774,
		944711139,
		264347078,
		2341262773,
		604807628,
		2007800933,
		770255983,
		1495990901,
		1249150122,
		1856431235,
		1555081692,
		3175218132,
		1996064986,
		2198950837,
		2554220882,
		3999719339,
		2821834349,
		766784016,
		2952996808,
		2566594879,
		3210313671,
		3203337956,
		3336571891,
		1034457026,
		3584528711,
		2466948901,
		113926993,
		3758326383,
		338241895,
		168717936,
		666307205,
		1188179964,
		773529912,
		1546045734,
		1294757372,
		1522805485,
		1396182291,
		2643833823,
		1695183700,
		2343527390,
		1986661051,
		1014477480,
		2177026350,
		1206759142,
		2456956037,
		344077627,
		2730485921,
		1290863460,
		2820302411,
		3158454273,
		3259730800,
		3505952657,
		3345764771,
		106217008,
		3516065817,
		3606008344,
		3600352804,
		1432725776,
		4094571909,
		1467031594,
		275423344,
		851169720,
		430227734,
		3100823752,
		506948616,
		1363258195,
		659060556,
		3750685593,
		883997877,
		3785050280,
		958139571,
		3318307427,
		1322822218,
		3812723403,
		1537002063,
		2003034995,
		1747873779,
		3602036899,
		1955562222,
		1575990012,
		2024104815,
		1125592928,
		2227730452,
		2716904306,
		2361852424,
		442776044,
		2428436474,
		593698344,
		2756734187,
		3733110249,
		3204031479,
		2999351573,
		3329325298,
		3815920427,
		3391569614,
		3928383900,
		3515267271,
		566280711,
		3940187606,
		3454069534,
		4118630271,
		4000239992,
		116418474,
		1914138554,
		174292421,
		2731055270,
		289380356,
		3203993006,
		460393269,
		320620315,
		685471733,
		587496836,
		852142971,
		1086792851,
		1017036298,
		365543100,
		1126000580,
		2618297676,
		1288033470,
		3409855158,
		1501505948,
		4234509866,
		1607167915,
		987167468,
		1816402316,
		1246189591
	];
	var W = new Array(160);
	function Sha512() {
		this.init();
		this._w = W;
		Hash.call(this, 128, 112);
	}
	inherits(Sha512, Hash);
	Sha512.prototype.init = function() {
		this._ah = 1779033703;
		this._bh = 3144134277;
		this._ch = 1013904242;
		this._dh = 2773480762;
		this._eh = 1359893119;
		this._fh = 2600822924;
		this._gh = 528734635;
		this._hh = 1541459225;
		this._al = 4089235720;
		this._bl = 2227873595;
		this._cl = 4271175723;
		this._dl = 1595750129;
		this._el = 2917565137;
		this._fl = 725511199;
		this._gl = 4215389547;
		this._hl = 327033209;
		return this;
	};
	function Ch(x, y, z) {
		return z ^ x & (y ^ z);
	}
	function maj(x, y, z) {
		return x & y | z & (x | y);
	}
	function sigma0(x, xl) {
		return (x >>> 28 | xl << 4) ^ (xl >>> 2 | x << 30) ^ (xl >>> 7 | x << 25);
	}
	function sigma1(x, xl) {
		return (x >>> 14 | xl << 18) ^ (x >>> 18 | xl << 14) ^ (xl >>> 9 | x << 23);
	}
	function Gamma0(x, xl) {
		return (x >>> 1 | xl << 31) ^ (x >>> 8 | xl << 24) ^ x >>> 7;
	}
	function Gamma0l(x, xl) {
		return (x >>> 1 | xl << 31) ^ (x >>> 8 | xl << 24) ^ (x >>> 7 | xl << 25);
	}
	function Gamma1(x, xl) {
		return (x >>> 19 | xl << 13) ^ (xl >>> 29 | x << 3) ^ x >>> 6;
	}
	function Gamma1l(x, xl) {
		return (x >>> 19 | xl << 13) ^ (xl >>> 29 | x << 3) ^ (x >>> 6 | xl << 26);
	}
	function getCarry(a, b) {
		return a >>> 0 < b >>> 0 ? 1 : 0;
	}
	Sha512.prototype._update = function(M) {
		var w = this._w;
		var ah = this._ah | 0;
		var bh = this._bh | 0;
		var ch = this._ch | 0;
		var dh = this._dh | 0;
		var eh = this._eh | 0;
		var fh = this._fh | 0;
		var gh = this._gh | 0;
		var hh = this._hh | 0;
		var al = this._al | 0;
		var bl = this._bl | 0;
		var cl = this._cl | 0;
		var dl = this._dl | 0;
		var el = this._el | 0;
		var fl = this._fl | 0;
		var gl = this._gl | 0;
		var hl = this._hl | 0;
		for (var i = 0; i < 32; i += 2) {
			w[i] = M.readInt32BE(i * 4);
			w[i + 1] = M.readInt32BE(i * 4 + 4);
		}
		for (; i < 160; i += 2) {
			var xh = w[i - 30];
			var xl = w[i - 30 + 1];
			var gamma0 = Gamma0(xh, xl);
			var gamma0l = Gamma0l(xl, xh);
			xh = w[i - 4];
			xl = w[i - 4 + 1];
			var gamma1 = Gamma1(xh, xl);
			var gamma1l = Gamma1l(xl, xh);
			var Wi7h = w[i - 14];
			var Wi7l = w[i - 14 + 1];
			var Wi16h = w[i - 32];
			var Wi16l = w[i - 32 + 1];
			var Wil = gamma0l + Wi7l | 0;
			var Wih = gamma0 + Wi7h + getCarry(Wil, gamma0l) | 0;
			Wil = Wil + gamma1l | 0;
			Wih = Wih + gamma1 + getCarry(Wil, gamma1l) | 0;
			Wil = Wil + Wi16l | 0;
			Wih = Wih + Wi16h + getCarry(Wil, Wi16l) | 0;
			w[i] = Wih;
			w[i + 1] = Wil;
		}
		for (var j = 0; j < 160; j += 2) {
			Wih = w[j];
			Wil = w[j + 1];
			var majh = maj(ah, bh, ch);
			var majl = maj(al, bl, cl);
			var sigma0h = sigma0(ah, al);
			var sigma0l = sigma0(al, ah);
			var sigma1h = sigma1(eh, el);
			var sigma1l = sigma1(el, eh);
			var Kih = K[j];
			var Kil = K[j + 1];
			var chh = Ch(eh, fh, gh);
			var chl = Ch(el, fl, gl);
			var t1l = hl + sigma1l | 0;
			var t1h = hh + sigma1h + getCarry(t1l, hl) | 0;
			t1l = t1l + chl | 0;
			t1h = t1h + chh + getCarry(t1l, chl) | 0;
			t1l = t1l + Kil | 0;
			t1h = t1h + Kih + getCarry(t1l, Kil) | 0;
			t1l = t1l + Wil | 0;
			t1h = t1h + Wih + getCarry(t1l, Wil) | 0;
			var t2l = sigma0l + majl | 0;
			var t2h = sigma0h + majh + getCarry(t2l, sigma0l) | 0;
			hh = gh;
			hl = gl;
			gh = fh;
			gl = fl;
			fh = eh;
			fl = el;
			el = dl + t1l | 0;
			eh = dh + t1h + getCarry(el, dl) | 0;
			dh = ch;
			dl = cl;
			ch = bh;
			cl = bl;
			bh = ah;
			bl = al;
			al = t1l + t2l | 0;
			ah = t1h + t2h + getCarry(al, t1l) | 0;
		}
		this._al = this._al + al | 0;
		this._bl = this._bl + bl | 0;
		this._cl = this._cl + cl | 0;
		this._dl = this._dl + dl | 0;
		this._el = this._el + el | 0;
		this._fl = this._fl + fl | 0;
		this._gl = this._gl + gl | 0;
		this._hl = this._hl + hl | 0;
		this._ah = this._ah + ah + getCarry(this._al, al) | 0;
		this._bh = this._bh + bh + getCarry(this._bl, bl) | 0;
		this._ch = this._ch + ch + getCarry(this._cl, cl) | 0;
		this._dh = this._dh + dh + getCarry(this._dl, dl) | 0;
		this._eh = this._eh + eh + getCarry(this._el, el) | 0;
		this._fh = this._fh + fh + getCarry(this._fl, fl) | 0;
		this._gh = this._gh + gh + getCarry(this._gl, gl) | 0;
		this._hh = this._hh + hh + getCarry(this._hl, hl) | 0;
	};
	Sha512.prototype._hash = function() {
		var H = Buffer.allocUnsafe(64);
		function writeInt64BE(h, l, offset) {
			H.writeInt32BE(h, offset);
			H.writeInt32BE(l, offset + 4);
		}
		writeInt64BE(this._ah, this._al, 0);
		writeInt64BE(this._bh, this._bl, 8);
		writeInt64BE(this._ch, this._cl, 16);
		writeInt64BE(this._dh, this._dl, 24);
		writeInt64BE(this._eh, this._el, 32);
		writeInt64BE(this._fh, this._fl, 40);
		writeInt64BE(this._gh, this._gl, 48);
		writeInt64BE(this._hh, this._hl, 56);
		return H;
	};
	module.exports = Sha512;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/sha384.js
var require_sha384 = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	var inherits = require_inherits_browser();
	var SHA512 = require_sha512();
	var Hash = require_hash();
	var Buffer = require_safe_buffer().Buffer;
	var W = new Array(160);
	function Sha384() {
		this.init();
		this._w = W;
		Hash.call(this, 128, 112);
	}
	inherits(Sha384, SHA512);
	Sha384.prototype.init = function() {
		this._ah = 3418070365;
		this._bh = 1654270250;
		this._ch = 2438529370;
		this._dh = 355462360;
		this._eh = 1731405415;
		this._fh = 2394180231;
		this._gh = 3675008525;
		this._hh = 1203062813;
		this._al = 3238371032;
		this._bl = 914150663;
		this._cl = 812702999;
		this._dl = 4144912697;
		this._el = 4290775857;
		this._fl = 1750603025;
		this._gl = 1694076839;
		this._hl = 3204075428;
		return this;
	};
	Sha384.prototype._hash = function() {
		var H = Buffer.allocUnsafe(48);
		function writeInt64BE(h, l, offset) {
			H.writeInt32BE(h, offset);
			H.writeInt32BE(l, offset + 4);
		}
		writeInt64BE(this._ah, this._al, 0);
		writeInt64BE(this._bh, this._bl, 8);
		writeInt64BE(this._ch, this._cl, 16);
		writeInt64BE(this._dh, this._dl, 24);
		writeInt64BE(this._eh, this._el, 32);
		writeInt64BE(this._fh, this._fl, 40);
		return H;
	};
	module.exports = Sha384;
}));
//#endregion
//#region ../../../../node_modules/.pnpm/sha.js@2.4.12/node_modules/sha.js/index.js
var require_sha_js = /* @__PURE__ */ __commonJSMin(((exports, module) => {
	module.exports = function SHA(algorithm) {
		var alg = algorithm.toLowerCase();
		var Algorithm = module.exports[alg];
		if (!Algorithm) throw new Error(alg + " is not supported (we accept pull requests)");
		return new Algorithm();
	};
	module.exports.sha = require_sha();
	module.exports.sha1 = require_sha1();
	module.exports.sha224 = require_sha224();
	module.exports.sha256 = require_sha256();
	module.exports.sha384 = require_sha384();
	module.exports.sha512 = require_sha512();
}));
//#endregion
export default require_sha_js();
