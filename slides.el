;; adapted from: https://www.youtube.com/watch?v=TMoPuv-xXMM

(defun next-slide (&optional dir)
  (interactive)
  (let* ((parts (split-string (buffer-file-name) "/"))
         (dirs (butlast parts))
         (name (car (last parts)))
         (cur (string-to-number (car (split-string name "-"))))
         (next (+ cur (or dir 1)))
         (pat (concat (mapconcat 'identity dirs "/")
                      (format "/%03d-*" next)))
         (match (car (file-expand-wildcards pat))))
    (and match (find-file match))))

(defun prev-slide ()
  (interactive)
  (next-slide -1))

(global-set-key (kbd "<next>") 'next-slide)
(global-set-key (kbd "<prior>") 'prev-slide)
