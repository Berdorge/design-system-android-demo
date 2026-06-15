package android.content.res

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi

internal open class TypedArrayDecorator(private val delegate: TypedArray) : TypedArray() {
    override fun close() {
        delegate.close()
    }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean {
        return delegate.getBoolean(index, defValue)
    }

    override fun getChangingConfigurations(): Int {
        return delegate.changingConfigurations
    }

    override fun getColor(index: Int, defValue: Int): Int {
        return delegate.getColor(index, defValue)
    }

    override fun getColorStateList(index: Int): ColorStateList? {
        return delegate.getColorStateList(index)
    }

    override fun getDimension(index: Int, defValue: Float): Float {
        return delegate.getDimension(index, defValue)
    }

    override fun getDimensionPixelOffset(index: Int, defValue: Int): Int {
        return delegate.getDimensionPixelOffset(index, defValue)
    }

    override fun getDimensionPixelSize(index: Int, defValue: Int): Int {
        return delegate.getDimensionPixelSize(index, defValue)
    }

    override fun getDrawable(index: Int): Drawable? {
        return delegate.getDrawable(index)
    }

    override fun getFloat(index: Int, defValue: Float): Float {
        return delegate.getFloat(index, defValue)
    }

    override fun getFont(index: Int): Typeface? {
        return delegate.getFont(index)
    }

    override fun getFraction(
        index: Int,
        base: Int,
        pbase: Int,
        defValue: Float
    ): Float {
        return delegate.getFraction(index, base, pbase, defValue)
    }

    override fun getIndex(at: Int): Int {
        return delegate.getIndex(at)
    }

    override fun getIndexCount(): Int = delegate.indexCount
    override fun getInt(index: Int, defValue: Int): Int {
        return delegate.getInt(index, defValue)
    }

    override fun getInteger(index: Int, defValue: Int): Int {
        return delegate.getInteger(index, defValue)
    }

    override fun getLayoutDimension(index: Int, defValue: Int): Int {
        return delegate.getLayoutDimension(index, defValue)
    }

    override fun getLayoutDimension(index: Int, name: String?): Int {
        return delegate.getLayoutDimension(index, name)
    }

    override fun getNonResourceString(index: Int): String? {
        return delegate.getNonResourceString(index)
    }

    override fun getPositionDescription(): String? {
        return delegate.getPositionDescription()
    }

    override fun getResourceId(index: Int, defValue: Int): Int {
        return delegate.getResourceId(index, defValue)
    }

    override fun getResources(): Resources? {
        return delegate.getResources()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getSourceResourceId(index: Int, defaultValue: Int): Int {
        return delegate.getSourceResourceId(index, defaultValue)
    }

    override fun getString(index: Int): String? {
        return delegate.getString(index)
    }

    override fun getText(index: Int): CharSequence? {
        return delegate.getText(index)
    }

    override fun getTextArray(index: Int): Array<out CharSequence?>? {
        return delegate.getTextArray(index)
    }

    override fun getType(index: Int): Int {
        return delegate.getType(index)
    }

    override fun getValue(index: Int, outValue: TypedValue?): Boolean {
        return delegate.getValue(index, outValue)
    }

    override fun hasValue(index: Int): Boolean {
        return delegate.hasValue(index)
    }

    override fun hasValueOrEmpty(index: Int): Boolean {
        return delegate.hasValueOrEmpty(index)
    }

    override fun length(): Int {
        return delegate.length()
    }

    override fun peekValue(index: Int): TypedValue? {
        return delegate.peekValue(index)
    }

    override fun recycle() {
        delegate.recycle()
    }

    override fun toString(): String {
        return delegate.toString()
    }
}
