/**
 * BSD Zero Clause License
 *
 * Copyright (c) 2012 Karthikeyan Natarajan (karthikeyan21@gmail.com)
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */
package com.editor.track;

public class Vector {

	public float x;
	public float y;

	public Vector(){
	}
	
	public Vector(float x,float y) {
		this.x = x;
		this.y = y;
	}

	public float magnitude() {
		return (float)Math.sqrt((double)(x*x+y*y));
	}

	public static Vector add(Vector L, Vector R){
		return new Vector(L.x + R.x, L.y + R.y);
	}
	
	public static Vector substract(Vector L, Vector R){
		return new Vector(L.x - R.x, L.y - R.y);
	}

	public static Vector negative(Vector r) {
		return new Vector(-r.x,-r.y);
	}

    public static Vector multiply(Vector L, float R) {
        return new Vector(L.x * R, L.y * R);
    }

    //divide multiply
    public static Vector divide(Vector L, float R) {
        return new Vector(L.x / R, L.y / R);
    }

    //dot product
    public static float dot(Vector L, Vector R)  {
        return (L.x * R.x + L.y * R.y);
    }

	//cross product, in 2d this is a scalar since we know it points in the Z direction
    public static float cross(Vector L, Vector R) {
        return (L.x*R.y - L.y*R.x);
    }

    //normalize the vector
    public static Vector normalize(Vector vector) {
        float mag = vector.magnitude();
        float x = vector.x / mag;
        float y = vector.y / mag;
        return new Vector(x,y);
    }

    //project this vector on to v
    public Vector Project(Vector v) {
        //projected vector = (this dot v) * v;
        float thisDotV = dot(this ,v);
        return multiply(v,thisDotV);
    }

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("X: ").append(x).append(", Y: ").append(y);
    	return sb.toString();
    }
    
}
