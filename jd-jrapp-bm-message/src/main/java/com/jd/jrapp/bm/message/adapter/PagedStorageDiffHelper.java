

package com.jd.jrapp.bm.message.adapter;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

im

import java.util.List;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/19
 * Author:wangkai
 * Description:
 * =====================================================
 */
class PagedStorageDiffHelper {
    private PagedStorageDiffHelper() {
    }



    static <T> DiffUtil.DiffResult computeDiff(
            final List<T> oldList,
            final List<T> newList,
            final DiffUtil.ItemCallback<T> diffCallback) {


        final int oldSize = oldList.size();
        final int newSize = newList.size();

        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition );
                T newItem = newList.get(newItemPosition );
                if (oldItem == null || newItem == null) {
                    return null;
                }
                return diffCallback.getChangePayload(oldItem, newItem);
            }

            @Override
            public int getOldListSize() {
                return oldSize;
            }

            @Override
            public int getNewListSize() {
                return newSize;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition);
                T newItem = newList.get(newItemPosition );
                if (oldItem == newItem) {
                    return true;
                }
                //noinspection SimplifiableIfStatement
                if (oldItem == null || newItem == null) {
                    return false;
                }
                return diffCallback.areItemsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition);
                T newItem = newList.get(newItemPosition);
                if (oldItem == newItem) {
                    return true;
                }
                //noinspection SimplifiableIfStatement
                if (oldItem == null || newItem == null) {
                    return false;
                }

                return diffCallback.areContentsTheSame(oldItem, newItem);
            }
        }, false);
    }

    private static class OffsettingListUpdateCallback implements ListUpdateCallback {
        private final int mOffset;
        private final ListUpdateCallback mCallback;

        private OffsettingListUpdateCallback(int offset, ListUpdateCallback callback) {
            mOffset = offset;
            mCallback = callback;
        }

        @Override
        public void onInserted(int position, int count) {
            mCallback.onInserted(position + mOffset, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            mCallback.onRemoved(position + mOffset, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            mCallback.onRemoved(fromPosition + mOffset, toPosition + mOffset);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            mCallback.onChanged(position + mOffset, count, payload);
        }
    }

}
